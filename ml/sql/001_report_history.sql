-- Seat-Scout ML, step 1: keep a history of condition reports.
--
-- public.reports (android/supabase/reports.sql) keeps one current report per
-- user and area, so an upsert replaces the previous report. The estimate model
-- needs every report, so a trigger copies each insert or update on
-- public.reports into the append-only table public.report_events.
-- The app's write path does not change.
--
-- Run after android/supabase/reports.sql. Safe to run more than once.

-- 1. Append-only report history ---------------------------------------------

create table if not exists public.report_events (
  id          bigint generated always as identity primary key,
  area_id     bigint not null references public.study_area (id),
  seats       text not null check (seats in ('MANY', 'FEW', 'FULL')),
  atmosphere  text not null check (atmosphere in ('QUIET', 'MODERATE', 'NOISY')),
  reporter    uuid references auth.users (id) on delete cascade,
  reported_at timestamptz not null default now()      -- server time
);

create index if not exists report_events_area_time_idx
  on public.report_events (area_id, reported_at desc);
create index if not exists report_events_time_idx
  on public.report_events (reported_at desc);

-- Index the reporter for account deletion cascades.
create index if not exists report_events_reporter_idx on public.report_events (reporter);

-- Raw history includes reporter IDs, so clients get no direct access.
-- The trigger below and estimate_conditions() run as the table owner.
alter table public.report_events enable row level security;
revoke all on public.report_events from anon, authenticated;

-- 2. Copy every report into the history ------------------------------------
-- This trigger never blocks or fails the student's write to public.reports.
-- A report it cannot interpret is skipped with a warning in the Postgres log.

create or replace function public.log_report_event()
returns trigger
language plpgsql
security definer
set search_path = public, pg_temp
as $$
declare
  v_area bigint;
begin
  if new.area_id !~ '^[0-9]{1,18}$' then
    raise warning 'report_events: skipped report for area %', new.area_id;
    return null;
  end if;
  select id into v_area from study_area where id = new.area_id::bigint;
  if v_area is null then
    raise warning 'report_events: skipped report for unknown area %', new.area_id;
    return null;
  end if;

  insert into report_events (area_id, seats, atmosphere, reporter)
  values (v_area, new.seats, new.atmosphere, new.user_id);
  return null;
exception when others then
  raise warning 'report_events: %', sqlerrm;
  return null;
end;
$$;

revoke all on function public.log_report_event() from public, anon, authenticated;

drop trigger if exists report_events_log on public.reports;
create trigger report_events_log
  after insert or update on public.reports
  for each row execute function public.log_report_event();
