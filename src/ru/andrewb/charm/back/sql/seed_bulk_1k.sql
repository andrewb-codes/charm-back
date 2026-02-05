/*
 insert 1000 test users with passwords:
 user<id>@charm.ru - password
*/

WITH params AS (
    SELECT 1000::int AS n
),
base AS (
    SELECT coalesce(max(id),0)+1 AS start_id
    FROM profile
)
INSERT INTO profile (email, password, "name", surname, birthdate, about, gender, status, role)
SELECT
    'user' || (b.start_id + g.i)::text || '@mail.ru',
    '$2a$10$1CmwmzBGXw1nulcCNyXUZ.34Pq5K/QKJJ.wzWpC1Um5YICgZVRZ4a',
    (ARRAY['Ivan','Petr','Andrei','Sergey','Alex','Dmitry','Elena','Olga','Maria','Anna'])[
        1 + floor(random()*10)::int
    ],
    (ARRAY['Ivanov','Petrov','Sidorov','Smirnov','Kuznetsov','Popov','Sidorova','Smirnova','Kузнецова','Попова'])[
        1 + floor(random()*10)::int
    ],
    (
        current_date
        - ((18 + floor(random()*43)::int) * INTERVAL '1 year')
        - (floor(random()*365)::int * INTERVAL '1 day')
    )::date,
    ('About user #' || (b.start_id + g.i)),
    (ARRAY['MALE', 'FEMALE'])[1 + floor(random()*2)::int],
    (ARRAY['ACTIVE', 'INACTIVE'])[1 + floor(random()*2)::int],
    CASE WHEN random() < 0.05 THEN 'ADMIN' ELSE 'USER' END
FROM params p
CROSS JOIN base b
CROSS JOIN LATERAL pg_catalog.generate_series(0, p.n - 1) AS g(i);