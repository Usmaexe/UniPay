-- Insert Roles (skip if UUID/role already exists)
INSERT INTO roles (id, name, description, created_at, updated_at, is_deleted)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'ADMIN', 'Administrator role', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    ('22222222-2222-2222-2222-222222222222', 'USER', 'Standard user role', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    ('33333333-3333-3333-3333-333333333333', 'CLIENT', 'Client role', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false)
ON CONFLICT (id) DO NOTHING;

-- Insert Permissions (skip if UUID/permission already exists)
INSERT INTO permissions (id, name, description, created_at, updated_at, is_deleted)
VALUES
    ('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', 'VIEW_PROFILE', 'View Profile', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    ('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', 'UPDATE_PROFILE', 'Update Profile', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    ('aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3', 'VIEW_ORDERS', 'View Orders', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    ('aaaaaaa4-aaaa-aaaa-aaaa-aaaaaaaaaaa4', 'CREATE_ORDER', 'Create Order', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    ('aaaaaaa5-aaaa-aaaa-aaaa-aaaaaaaaaaa5', 'MANAGE_USERS', 'Manage Users', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false),
    ('aaaaaaa6-aaaa-aaaa-aaaa-aaaaaaaaaaa6', 'VIEW_DASHBOARD', 'View Dashboard', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false)
ON CONFLICT (id) DO NOTHING;

-- Associate Permissions with Roles (skip existing pairs)
-- ADMIN Role
INSERT INTO role_permissions (role_id, permission_id)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'aaaaaaa5-aaaa-aaaa-aaaa-aaaaaaaaaaa5'),
    ('11111111-1111-1111-1111-111111111111', 'aaaaaaa6-aaaa-aaaa-aaaa-aaaaaaaaaaa6')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- USER Role
INSERT INTO role_permissions (role_id, permission_id)
VALUES
    ('22222222-2222-2222-2222-222222222222', 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1'),
    ('22222222-2222-2222-2222-222222222222', 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2'),
    ('22222222-2222-2222-2222-222222222222', 'aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3'),
    ('22222222-2222-2222-2222-222222222222', 'aaaaaaa4-aaaa-aaaa-aaaa-aaaaaaaaaaa4')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- CLIENT Role
INSERT INTO role_permissions (role_id, permission_id)
VALUES
    ('33333333-3333-3333-3333-333333333333', 'aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3'),
    ('33333333-3333-3333-3333-333333333333', 'aaaaaaa4-aaaa-aaaa-aaaa-aaaaaaaaaaa4'),
    ('33333333-3333-3333-3333-333333333333', 'aaaaaaa6-aaaa-aaaa-aaaa-aaaaaaaaaaa6')
ON CONFLICT (role_id, permission_id) DO NOTHING;