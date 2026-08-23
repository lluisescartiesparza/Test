-- Enable Row Level Security
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.teams ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.team_members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.events ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance ENABLE ROW LEVEL SECURITY;

-- Note: The app currently uses a MockSessionManager and connects as 'anon'.
-- These policies are intentionally open for 'anon' to prevent breaking the app 
-- until Supabase Auth is implemented in the client.
CREATE POLICY "Allow anon read access" ON public.users FOR SELECT TO anon USING (true);
CREATE POLICY "Allow anon read access" ON public.teams FOR SELECT TO anon USING (true);
CREATE POLICY "Allow anon read access" ON public.team_members FOR SELECT TO anon USING (true);
CREATE POLICY "Allow anon read access" ON public.events FOR SELECT TO anon USING (true);
CREATE POLICY "Allow anon read access" ON public.attendance FOR SELECT TO anon USING (true);

CREATE POLICY "Allow anon insert access" ON public.users FOR INSERT TO anon WITH CHECK (true);
CREATE POLICY "Allow anon insert access" ON public.teams FOR INSERT TO anon WITH CHECK (true);
CREATE POLICY "Allow anon insert access" ON public.team_members FOR INSERT TO anon WITH CHECK (true);
CREATE POLICY "Allow anon insert access" ON public.events FOR INSERT TO anon WITH CHECK (true);
CREATE POLICY "Allow anon insert access" ON public.attendance FOR INSERT TO anon WITH CHECK (true);

CREATE POLICY "Allow anon update access" ON public.users FOR UPDATE TO anon USING (true) WITH CHECK (true);
CREATE POLICY "Allow anon update access" ON public.teams FOR UPDATE TO anon USING (true) WITH CHECK (true);
CREATE POLICY "Allow anon update access" ON public.team_members FOR UPDATE TO anon USING (true) WITH CHECK (true);
CREATE POLICY "Allow anon update access" ON public.events FOR UPDATE TO anon USING (true) WITH CHECK (true);
CREATE POLICY "Allow anon update access" ON public.attendance FOR UPDATE TO anon USING (true) WITH CHECK (true);

CREATE POLICY "Allow anon delete access" ON public.users FOR DELETE TO anon USING (true);
CREATE POLICY "Allow anon delete access" ON public.teams FOR DELETE TO anon USING (true);
CREATE POLICY "Allow anon delete access" ON public.team_members FOR DELETE TO anon USING (true);
CREATE POLICY "Allow anon delete access" ON public.events FOR DELETE TO anon USING (true);
CREATE POLICY "Allow anon delete access" ON public.attendance FOR DELETE TO anon USING (true);
