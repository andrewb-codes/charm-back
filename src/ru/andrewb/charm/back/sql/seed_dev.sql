truncate table profile_like;
truncate table profile restart identity cascade;

insert into profile (email, password, name, surname, birthdate, about, gender, status, role) values
('admin@charm.ru', 'qwerty', 'Admin', null, null, null, null, 'INACTIVE', 'ADMIN'),
('ivanov@mail.ru', '123', 'Ivan', 'Ivanov', '2001-12-03', 'I am QA', 'MALE','ACTIVE', 'USER'),
('sidorova@mail.ru', '456', 'Elena', 'Sidorova', '1999-09-01', 'I am Java Dev', 'FEMALE', 'ACTIVE', 'USER');

insert into profile_like (from_profile, to_profile, is_like, is_match)
select p1."id", p2."id", true, false
from profile p1, profile p2
where p1.email='ivanov@mail.ru' and p2.email='sidorova@mail.ru'
union all
select p2."id", p1."id", false, false
from profile p1, profile p2
where p1.email='ivanov@mail.ru' and p2.email='sidorova@mail.ru';