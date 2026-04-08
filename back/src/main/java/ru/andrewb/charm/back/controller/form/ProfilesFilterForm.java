package ru.andrewb.charm.back.controller.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfilesFilterForm {
    private String emailStartsWith;
    private String nameStartsWith;
    private String surnameStartsWith;
    private String ltAge;
    private String gteAge;
    private String role;
    private String status;

    private String sortBy;
    private String sortOrder;

    private String page;
    private String pageSize;
}
