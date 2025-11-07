package ru.andrewb.charm.back.utils;

import ru.andrewb.charm.back.dto.ProfileFilter;


public class BeanUtils {

    public static ProfileFilter copyProperties(ProfileFilter f, ProfileFilter copy) {
        copy.setEmailStartsWith(f.getEmailStartsWith());
        copy.setNameStartsWith(f.getNameStartsWith());
        copy.setSurnameStartsWith(f.getSurnameStartsWith());
        copy.setLowerAgeBound(f.getLowerAgeBound());
        copy.setGreaterAndEqualAgeBound(f.getGreaterAndEqualAgeBound());
        copy.setRole(f.getRole());
        copy.setStatus(f.getStatus());
        copy.setSortBy(f.getSortBy());
        copy.setSortOrder(f.getSortOrder());
        copy.setPage(f.getPage());
        copy.setPageSize(f.getPageSize());
        return copy;
    }
}
