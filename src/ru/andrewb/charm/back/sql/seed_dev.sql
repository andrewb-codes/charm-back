TRUNCATE TABLE profile_like;
TRUNCATE TABLE profile RESTART IDENTITY CASCADE;

INSERT INTO profile (email, password, "name", surname, birthdate, about, gender, status, role)
VALUES
    ('admin@charm.ru',   'qwerty', 'Admin',  NULL,         NULL,               NULL,           NULL,     'INACTIVE','ADMIN'),
    ('ivanov@mail.ru',   '123',    'Ivan',   'Ivanov',     '2001-12-03'::DATE, 'I am QA',      'MALE',   'ACTIVE',  'USER'),
    ('sidorova@mail.ru', '456',    'Elena',  'Sidorova',   '1999-09-01'::DATE, 'I am Java Dev','FEMALE', 'ACTIVE',  'USER');

INSERT INTO profile_like (a_profile, b_profile, liked_a, liked_b)
SELECT LEAST(p1.id, p2.id), GREATEST(p1.id, p2.id), TRUE, TRUE
FROM profile p1, profile p2
WHERE p1.email = 'ivanov@mail.ru' AND p2.email = 'sidorova@mail.ru';

INSERT INTO profile_like (a_profile, b_profile, liked_a, liked_b)
SELECT LEAST(p1.id, p2.id), GREATEST(p1.id, p2.id),
       CASE WHEN p1.id < p2.id THEN TRUE ELSE NULL END,
       CASE WHEN p1.id < p2.id THEN NULL ELSE TRUE END
FROM profile p1, profile p2
WHERE p1.email = 'sidorova@mail.ru' AND p2.email = 'admin@charm.ru';