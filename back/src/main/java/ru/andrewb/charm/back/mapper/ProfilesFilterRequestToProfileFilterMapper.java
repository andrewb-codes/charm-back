package ru.andrewb.charm.back.mapper;

import org.springframework.stereotype.Component;
import ru.andrewb.charm.back.controller.request.ProfilesFilterRequest;
import ru.andrewb.charm.back.dto.ProfilesFilter;
import ru.andrewb.charm.back.normalizer.ProfilesFilterDefaults;

import static ru.andrewb.charm.back.utils.StringUtils.stripToNull;

@Component
public class ProfilesFilterRequestToProfileFilterMapper implements Mapper<ProfilesFilterRequest, ProfilesFilter> {

    @Override
    public ProfilesFilter map(ProfilesFilterRequest request) {
        return map(request, new ProfilesFilter());
    }

    @Override
    public ProfilesFilter map(ProfilesFilterRequest request, ProfilesFilter filter) {
        filter.setEmailStartsWith(stripToNull(request.getEmailStartsWith()));
        filter.setNameStartsWith(stripToNull(request.getNameStartsWith()));
        filter.setSurnameStartsWith(stripToNull(request.getSurnameStartsWith()));
        filter.setLowerAgeBound(request.getLtAge());
        filter.setGreaterAndEqualAgeBound(request.getGteAge());
        filter.setRole(request.getRole());
        filter.setStatus(request.getStatus());
        filter.setSortBy(request.getSortBy());
        filter.setSortOrder(request.getSortOrder());
        filter.setPage(request.getPage());
        filter.setPageSize(request.getPageSize());
        return ProfilesFilterDefaults.normalize(filter);
    }
}
