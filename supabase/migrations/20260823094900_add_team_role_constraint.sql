UPDATE team_members SET role = 'JUGADOR' WHERE role IS NULL OR role NOT IN ('ENTRENADOR', 'JUGADOR');
ALTER TABLE team_members ADD CONSTRAINT check_team_role CHECK (role IN ('ENTRENADOR', 'JUGADOR'));
