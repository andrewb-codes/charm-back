package ru.andrewb.charm.back.dto;

import ru.andrewb.charm.back.dto.sort.SortBy;
import ru.andrewb.charm.back.dto.sort.SortOrder;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.model.Status;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProfileSelectQueryBuilder {

    public static final String SELECT_BASE = """
            select id, email, password, "name", surname, birthdate, about, gender, photo, status, role, version
            from profile
            where '' = ''
            """;

    private final StringBuilder sb;
    private final List<Object> args;

    public ProfileSelectQueryBuilder() {
        this.sb = new StringBuilder(SELECT_BASE);
        this.args = new ArrayList<>();
    }

    public ProfileSelectQueryBuilder addIdFilter(Long id) {
        if (id == null) return this;
        sb.append(" and id = ?");
        args.add(id);
        return this;
    }

    public ProfileSelectQueryBuilder addEmailFilter(String email) {
        if (email == null) return this;
        sb.append(" and email = ?");
        args.add(email);
        return this;
    }

    public ProfileSelectQueryBuilder addPasswordFilter(String password) {
        if (password == null) return this;
        sb.append(" and password = ?");
        args.add(password);
        return this;
    }

    public ProfileSelectQueryBuilder addEmailStartsWithFilter(String emailStartsWith) {
        if (emailStartsWith == null) return this;
        sb.append(" and email ilike ?");
        args.add(emailStartsWith + "%");
        return this;
    }

    public ProfileSelectQueryBuilder addNameStartsWithFilter(String nameStartsWith) {
        if (nameStartsWith == null) return this;
        sb.append(" and \"name\" ilike ?");
        args.add(nameStartsWith + "%");
        return this;
    }

    public ProfileSelectQueryBuilder addSurnameStartsWithFilter(String surnameStartsWith) {
        if (surnameStartsWith == null) return this;
        sb.append(" and surname ilike ?");
        args.add(surnameStartsWith + "%");
        return this;
    }

    public ProfileSelectQueryBuilder addLowerAgeBound(Integer ageBound) {
        if (ageBound == null) return this;
        Date birthdateBound = Date.valueOf(LocalDate.now().minusYears(ageBound));
        sb.append(" and birthdate > ?");
        args.add(birthdateBound);
        return this;
    }

    public ProfileSelectQueryBuilder addGreaterAndEqualAgeBound(Integer ageBound) {
        if (ageBound == null) return this;
        Date birthdateBound = Date.valueOf(LocalDate.now().minusYears(ageBound));
        sb.append(" and birthdate <= ?");
        args.add(birthdateBound);
        return this;
    }

    public ProfileSelectQueryBuilder addRoleFilter(Role role) {
        if (role == null) return this;
        sb.append(" and role = ?");
        args.add(role.toString());
        return this;
    }

    public ProfileSelectQueryBuilder addStatusFilter(Status status) {
        if (status == null) return this;
        sb.append(" and status = ?");
        args.add(status.toString());
        return this;
    }

    public ProfileSelectQueryBuilder orderBy(SortBy sortBy, SortOrder sortOrder) {
        if (sortBy == SortBy.BIRTHDATE) {
            sortOrder = sortOrder.flip();
        }

        sb.append(" order by ").append(sortBy.getColumn()).append(' ').append(sortOrder)
                .append(" nulls last");
        return this;
    }

    public ProfileSelectQueryBuilder pageAndPageSize(Integer page, Integer pageSize) {
        int limit = pageSize;
        int offset = limit * (page - 1);
        sb.append(" limit ? offset ?");
        args.add(limit);
        args.add(offset);
        return this;
    }

    public Query build() {
        return new Query(sb.toString(), args);
    }
}