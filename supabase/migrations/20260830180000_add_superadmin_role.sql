-- 1. Añadir el rol SUPERADMIN al enum user_role
ALTER TYPE user_role ADD VALUE IF NOT EXISTS 'SUPERADMIN';
