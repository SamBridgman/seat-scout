-- Seat-Scout ML, step 2: estimate current seating and atmosphere per room.
--
-- Model (plan A): hierarchical Bayesian smoothing plus live reports.
--   1. History: reports older than 60 minutes with the same day type
--      (weekday or weekend) count toward the pattern. Each one is weighted by
--      how close its local time of day is to the target time (Gaussian,
--      sigma 1 hour, cut off at 3 hours) and by its age (half-life 28 days).
--   2. Smoothing: global counts with a uniform Dirichlet(1,1,1) prior give
--      p_global. Zone counts are shrunk toward p_global, and room counts are
--      shrunk toward p_zone, each with a prior strength of 4 reports.
--   3. Live: reports from the past 60 minutes are added with weight
--      4 * 0.5^(age / 15 min) on top of the room pattern (prior strength 2).
--   basis = 'live' when the live weight is at least 1 (a report within about
--   30 minutes), 'pattern' when the room has a history weight of at least 3,
--   and 'insufficient' otherwise. 'insufficient' estimates must not be shown
--   as conditions.
--
-- ml/seatscout_model.py implements the same model. Keep the two in sync;
-- ml/tests/sql_parity.mjs checks that they agree.
--
-- Call from the app through PostgREST:
--   POST /rest/v1/rpc/estimate_conditions   body {}   (or {"at_time": "..."})
-- area_id is study_area.id as text, the same value as StudyArea.id in the app.

drop function if exists public.estimate_conditions(timestamptz);
create function public.estimate_conditions(at_time timestamptz default now())
returns table (
  area_id         text,              -- study_area.id, as used by the app
  dimension       text,              -- 'seats' or 'atmosphere'
  label           text,              -- most likely value
  probability     double precision,  -- probability of label
  probabilities   jsonb,             -- every value, rounded to 4 decimals
  basis           text,              -- 'live', 'pattern' or 'insufficient'
  history_weight  double precision,  -- effective number of pattern reports
  history_reports integer,           -- pattern reports within the time window
  live_reports    integer            -- reports in the past 60 minutes
)
language sql
stable
security definer
set search_path = public, pg_temp
as $$
with params as (
  select
    'America/New_York'::text as tz,
    1.0::float8  as hour_sigma,
    3.0::float8  as hour_window,
    28.0::float8 as history_half_life_days,
    60.0::float8 as live_window_min,
    15.0::float8 as live_half_life_min,
    4.0::float8  as live_weight,
    4.0::float8  as zone_strength,
    4.0::float8  as area_strength,
    2.0::float8  as pattern_strength,
    3.0::float8  as min_pattern_weight,
    1.0::float8  as min_live_weight
),
target as (
  select
    extract(isodow from l.ts) in (6, 7) as weekend,
    extract(hour from l.ts)::float8 + extract(minute from l.ts)::float8 / 60.0 as hour
  from params p
  cross join lateral (select at_time at time zone p.tz as ts) l
),
labels (dimension, label, ord) as (
  values ('seats', 'MANY', 1), ('seats', 'FEW', 2), ('seats', 'FULL', 3),
         ('atmosphere', 'QUIET', 1), ('atmosphere', 'MODERATE', 2), ('atmosphere', 'NOISY', 3)
),
events as (
  select
    e.area_id,
    a.zone,
    v.dimension,
    v.label,
    extract(epoch from (at_time - e.reported_at))::float8 as age_s,
    extract(isodow from l.ts) in (6, 7) as weekend,
    extract(hour from l.ts)::float8 + extract(minute from l.ts)::float8 / 60.0 as hour
  from report_events e
  join study_area a on a.id = e.area_id
  cross join params p
  cross join lateral (select e.reported_at at time zone p.tz as ts) l
  cross join lateral (values ('seats', e.seats), ('atmosphere', e.atmosphere)) v (dimension, label)
  where e.reported_at <= at_time
),
hist as (
  select ev.area_id, ev.zone, ev.dimension, ev.label,
         exp(-(d.hd * d.hd) / (2.0 * p.hour_sigma * p.hour_sigma))
           * power(0.5::float8, ev.age_s / 86400.0 / p.history_half_life_days) as w
  from events ev
  cross join params p
  cross join target t
  cross join lateral (select least(abs(ev.hour - t.hour), 24.0 - abs(ev.hour - t.hour)) as hd) d
  where ev.age_s >= p.live_window_min * 60.0
    and ev.weekend = t.weekend
    and d.hd <= p.hour_window
),
live as (
  select ev.area_id, ev.dimension, ev.label,
         p.live_weight * power(0.5::float8, ev.age_s / 60.0 / p.live_half_life_min) as w
  from events ev
  cross join params p
  where ev.age_s < p.live_window_min * 60.0
),
g_counts as (select h.dimension, h.label, sum(h.w) as c from hist h group by 1, 2),
z_counts as (select h.zone, h.dimension, h.label, sum(h.w) as c from hist h group by 1, 2, 3),
a_counts as (select h.area_id, h.dimension, h.label, sum(h.w) as c, count(*) as n from hist h group by 1, 2, 3),
l_counts as (select x.area_id, x.dimension, x.label, sum(x.w) as c, count(*) as n from live x group by 1, 2, 3),
cells as (
  select a.id as area_id, l.dimension, l.label, l.ord,
         coalesce(g.c, 0) as gc, coalesce(z.c, 0) as zc,
         coalesce(ac.c, 0) as ac, coalesce(ac.n, 0) as an,
         coalesce(lc.c, 0) as lc, coalesce(lc.n, 0) as ln
  from study_area a
  cross join labels l
  left join g_counts g on g.dimension = l.dimension and g.label = l.label
  left join z_counts z on z.zone = a.zone and z.dimension = l.dimension and z.label = l.label
  left join a_counts ac on ac.area_id = a.id and ac.dimension = l.dimension and ac.label = l.label
  left join l_counts lc on lc.area_id = a.id and lc.dimension = l.dimension and lc.label = l.label
),
totals as (
  select c.*,
         sum(c.gc) over w as g_tot, sum(c.zc) over w as z_tot,
         sum(c.ac) over w as a_tot, sum(c.an) over w as a_n,
         sum(c.lc) over w as l_tot, sum(c.ln) over w as l_n
  from cells c
  window w as (partition by c.area_id, c.dimension)
),
p_global as (
  select t.*, (t.gc + 1.0) / (t.g_tot + 3.0) as pg from totals t
),
p_zone as (
  select g.*, (g.zc + p.zone_strength * g.pg) / (g.z_tot + p.zone_strength) as pz
  from p_global g cross join params p
),
p_area as (
  select z.*, (z.ac + p.area_strength * z.pz) / (z.a_tot + p.area_strength) as pa
  from p_zone z cross join params p
),
posterior as (
  select a.*, (a.lc + p.pattern_strength * a.pa) / (a.l_tot + p.pattern_strength) as post,
         row_number() over (partition by a.area_id, a.dimension
                            order by (a.lc + p.pattern_strength * a.pa) / (a.l_tot + p.pattern_strength) desc,
                                     a.ord) as rnk
  from p_area a cross join params p
)
select
  top.area_id::text,
  top.dimension,
  top.label,
  top.post as probability,
  (select jsonb_object_agg(x.label, round(x.post::numeric, 4))
     from posterior x
    where x.area_id = top.area_id and x.dimension = top.dimension) as probabilities,
  case when top.l_tot >= p.min_live_weight then 'live'
       when top.a_tot >= p.min_pattern_weight then 'pattern'
       else 'insufficient' end as basis,
  top.a_tot as history_weight,
  top.a_n::integer as history_reports,
  top.l_n::integer as live_reports
from posterior top
cross join params p
where top.rnk = 1
order by top.area_id, top.dimension;
$$;

revoke all on function public.estimate_conditions(timestamptz) from public;
grant execute on function public.estimate_conditions(timestamptz) to anon, authenticated;
