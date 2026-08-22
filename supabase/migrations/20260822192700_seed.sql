-- Datos ficticios (Seed) para probar la app

-- Usuarios
INSERT INTO users (id, email, full_name, role) VALUES
    ('11111111-1111-1111-1111-111111111111', 'gerente@test.com', 'Gerente Test', 'GERENCIA'),
    ('22222222-2222-2222-2222-222222222222', 'director@test.com', 'Director Test', 'DIRECTOR_DEPORTIVO'),
    ('33333333-3333-3333-3333-333333333333', 'entrenador1@test.com', 'Entrenador Uno', 'ENTRENADOR'),
    ('44444444-4444-4444-4444-444444444444', 'jugador1@test.com', 'Jugador Uno', 'JUGADOR'),
    ('55555555-5555-5555-5555-555555555555', 'jugador2@test.com', 'Jugador Dos', 'JUGADOR');

-- Equipos
INSERT INTO teams (id, name, category) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Alevin A', 'Alevin'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Cadete B', 'Cadete');

-- Miembros del equipo
INSERT INTO team_members (team_id, user_id, role) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333', 'ENTRENADOR'),
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '44444444-4444-4444-4444-444444444444', 'JUGADOR'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '55555555-5555-5555-5555-555555555555', 'JUGADOR');

-- Eventos
INSERT INTO events (id, team_id, type, date) VALUES
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'ENTRENAMIENTO', '2026-08-25 18:00:00+02'),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'PARTIDO', '2026-08-28 10:00:00+02');

-- Asistencia
INSERT INTO attendance (event_id, user_id, status) VALUES
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', '44444444-4444-4444-4444-444444444444', 'CONFIRMADO'),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', '44444444-4444-4444-4444-444444444444', 'PENDING');
