with params as (select 1000::int as n),
base as (select coalesce(max(id),0)+1 as start_id from profile)
insert into profile (email, password, "name", surname, birthdate, about, gender, status, role)
select
    'user' || (b.start_id + gs)::text || '@mail.ru',
    'pass' || (b.start_id + gs)::text,
    (array['Ivan','Petr','Andrei','Sergey','Alex','Dmitry','Elena','Olga','Maria','Anna'])[
        1 + floor(random()*10)::int],
    (array['Ivanov','Petrov','Sidorov','Smirnov','Kuznetsov','Popov','Sidorova','Smirnova','Kuznetsova','Popova'])[
        1 + floor(random()*10)::int],
    (
        current_date
        - ( (18 + floor(random()*43)::int) * interval '1 year' )
        - ( floor(random()*365)::int * interval '1 day' )
    )::date,
    ('About user #' || (b.start_id + gs)),
    (array['MALE', 'FEMALE'])[1 + floor(random()*2)::int],
    (array['ACTIVE', 'INACTIVE'])[1 + floor(random()*2)::int],
    case when random() < 0.05 then 'ADMIN' else 'USER' end
from params p, base b, generate_series(0, p.n - 1) gs;