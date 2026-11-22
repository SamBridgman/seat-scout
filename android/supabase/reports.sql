-- Run once in Supabase SQL Editor. Enable anonymous sign-ins in Auth settings.
-- One current report per anonymous Auth user and area.
BEGIN;

CREATE TABLE public.reports (
    user_id uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    area_id text NOT NULL,
    seats text NOT NULL CHECK (seats IN ('MANY', 'FEW', 'FULL')),
    atmosphere text NOT NULL CHECK (atmosphere IN ('QUIET', 'MODERATE', 'NOISY')),
    saved_at bigint NOT NULL DEFAULT floor(extract(epoch FROM now()) * 1000)::bigint,
    PRIMARY KEY (user_id, area_id)
);

CREATE FUNCTION public.stamp_report_saved_at() RETURNS trigger
LANGUAGE plpgsql SET search_path = '' AS $$
BEGIN
    NEW.saved_at := floor(extract(epoch FROM statement_timestamp()) * 1000)::bigint;
    RETURN NEW;
END;
$$;
REVOKE ALL ON FUNCTION public.stamp_report_saved_at() FROM PUBLIC;
CREATE TRIGGER stamp_report_saved_at BEFORE INSERT OR UPDATE ON public.reports
    FOR EACH ROW EXECUTE FUNCTION public.stamp_report_saved_at();

ALTER TABLE public.reports ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON public.reports FROM anon, authenticated;
GRANT SELECT ON public.reports TO authenticated;
GRANT INSERT (user_id, area_id, seats, atmosphere) ON public.reports TO authenticated;
GRANT UPDATE (user_id, area_id, seats, atmosphere) ON public.reports TO authenticated;

CREATE POLICY "Read own reports" ON public.reports
    FOR SELECT TO authenticated USING ((SELECT auth.uid()) = user_id);
CREATE POLICY "Insert own reports" ON public.reports
    FOR INSERT TO authenticated WITH CHECK (
        (SELECT auth.uid()) = user_id AND EXISTS (
            SELECT 1 FROM public.study_area AS area WHERE area.id::text = area_id
        )
    );
CREATE POLICY "Update own reports" ON public.reports
    FOR UPDATE TO authenticated USING ((SELECT auth.uid()) = user_id)
    WITH CHECK ((SELECT auth.uid()) = user_id AND EXISTS (
        SELECT 1 FROM public.study_area AS area WHERE area.id::text = area_id
    ));
-- study_area's SELECT policy must also allow authenticated users to read areas.
COMMIT;
