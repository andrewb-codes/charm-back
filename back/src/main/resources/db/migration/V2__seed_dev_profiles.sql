INSERT INTO profile (email, password, "name", surname, birthdate, about, gender, status, role)
VALUES
    ('admin@charm.ru',   '$2a$10$lsHPyUkTiS1qKFwWqfGTrePIkDyA/r4TTD6O/bnG.oRNDDDm8aW92', 'Admin',  NULL,       NULL,               NULL,           NULL,     'INACTIVE','ADMIN'),
    ('ivanov@mail.ru',   '$2a$10$OCJYDgujqIh3NoG4GuCg/.rMsGmYF4SghfLnG3JUEKnmlrLaaFSU2', 'Ivan',   'Ivanov',   '2001-12-03'::DATE, 'I am QA',      'MALE',   'ACTIVE',  'USER'),
    ('sidorova@mail.ru', '$2a$10$CQX6kKadS86fpJypAb0jOemSdV3LNxhdxMQYSl0ijlOi8DfHjBiF6', 'Elena',  'Sidorova', '1999-09-01'::DATE, 'I am Java Dev','FEMALE', 'ACTIVE',  'USER');

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