ALTER TABLE team_members ADD CONSTRAINT check_team_role CHECK (role IN ('ENTRENADOR', 'JUGADOR'));
