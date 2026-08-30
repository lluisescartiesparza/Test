-- 1. Añadir el rol SUPERADMIN al enum user_role
ALTER TYPE user_role ADD VALUE IF NOT EXISTS 'SUPERADMIN';

-- 2. Asegurarse de que la extensión pgcrypto está habilitada (necesaria para el crypt de la contraseña)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 3. Crear el usuario superadmin en auth.users (Gestión de Supabase Auth)
INSERT INTO auth.users (
    instance_id,
    id,
    aud,
    role,
    email,
    encrypted_password,
    email_confirmed_at,
    raw_app_meta_data,
    raw_user_meta_data,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000000',
    '00000000-0000-0000-0000-000000000000',
    'authenticated',
    'authenticated',
    'lluisescartiesparza@gmail.com',
    crypt('superadmin', gen_salt('bf')),
    NOW(),
    '{"provider":"email","providers":["email"]}',
    '{}',
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;

-- 4. Enlazar la identidad de email en auth.identities
INSERT INTO auth.identities (
    id,
    user_id,
    identity_data,
    provider,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000000',
    '00000000-0000-0000-0000-000000000000',
    format('{"sub":"%s","email":"%s"}', '00000000-0000-0000-0000-000000000000', 'lluisescartiesparza@gmail.com')::jsonb,
    'email',
    NOW(),
    NOW()
) ON CONFLICT (provider, id) DO NOTHING;

-- 5. Crear el perfil público en la tabla users
INSERT INTO public.users (
    id,
    email,
    full_name,
    role
) VALUES (
    '00000000-0000-0000-0000-000000000000',
    'lluisescartiesparza@gmail.com',
    'Super Administrador',
    'SUPERADMIN'
) ON CONFLICT (id) DO NOTHING;
