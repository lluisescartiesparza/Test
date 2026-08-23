-- 1. Eliminar la restricción CHECK anterior
ALTER TABLE team_members DROP CONSTRAINT IF EXISTS check_team_role;

-- 2. Limpiar datos huérfanos o inválidos antes de la conversión
UPDATE team_members SET role = 'JUGADOR' WHERE role IS NULL OR role NOT IN ('ENTRENADOR', 'JUGADOR');

-- 3. Crear el nuevo tipo ENUM nativo
CREATE TYPE team_role AS ENUM ('ENTRENADOR', 'JUGADOR');

-- 4. Convertir la columna VARCHAR al nuevo tipo ENUM
ALTER TABLE team_members 
    ALTER COLUMN role TYPE team_role 
    USING role::team_role;
