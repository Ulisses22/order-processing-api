INSERT INTO roles (
    id,
    name,
    version,
    created_at,
    updated_at
)
VALUES (
           '11111111-1111-1111-1111-111111111111',
           'ROLE_ADMIN',
           0,
           NOW(),
           NOW()
       );

INSERT INTO users (
    id,
    username,
    password,
    enabled,
    version,
    created_at,
    updated_at
)
VALUES (
           '22222222-2222-2222-2222-222222222222',
           'admin',
           '$2a$10$7Tugw63XXhcwJ5AF5WMS..9il6MoQk5nzwnSNi6hDUEf0RZBGjgTK',
           TRUE,
           0,
           NOW(),
           NOW()
       );

INSERT INTO user_roles (
    user_id,
    role_id
)
VALUES (
           '22222222-2222-2222-2222-222222222222',
           '11111111-1111-1111-1111-111111111111'
       );
