package ru.andrewb.charm.back.normalizer;

import org.junit.jupiter.api.Test;
import ru.andrewb.charm.back.dto.ProfilesFilter;
import ru.andrewb.charm.back.dto.sort.SortBy;
import ru.andrewb.charm.back.dto.sort.SortOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ProfilesFilterDefaultsTest {

    @Test
    void normalize_shouldApplyAllDefaults_whenFieldsAreNull() {
        ProfilesFilter filter = new ProfilesFilter();

        ProfilesFilter result = ProfilesFilterDefaults.normalize(filter);

        assertSame(filter, result);
        assertEquals(SortBy.ID, filter.getSortBy());
        assertEquals(SortOrder.ASC, filter.getSortOrder());
        assertEquals(1, filter.getPage());
        assertEquals(10, filter.getPageSize());
    }

    @Test
    void normalize_shouldKeepExplicitSortSettings() {
        ProfilesFilter filter = new ProfilesFilter();
        filter.setSortBy(SortBy.EMAIL);
        filter.setSortOrder(SortOrder.DESC);
        filter.setPage(2);
        filter.setPageSize(20);

        ProfilesFilterDefaults.normalize(filter);

        assertEquals(SortBy.EMAIL, filter.getSortBy());
        assertEquals(SortOrder.DESC, filter.getSortOrder());
        assertEquals(2, filter.getPage());
        assertEquals(20, filter.getPageSize());
    }

    @Test
    void normalize_shouldSetDefaultPage_whenPageIsNull() {
        ProfilesFilter filter = new ProfilesFilter();
        filter.setPage(null);

        ProfilesFilterDefaults.normalize(filter);

        assertEquals(1, filter.getPage());
    }

    @Test
    void normalize_shouldSetDefaultPageSize_whenPageSizeIsNull() {
        ProfilesFilter filter = new ProfilesFilter();
        filter.setPageSize(null);

        ProfilesFilterDefaults.normalize(filter);

        assertEquals(10, filter.getPageSize());
    }

    @Test
    void normalize_shouldKeepPageSize_whenItIsAllowed() {
        ProfilesFilter filter = new ProfilesFilter();
        filter.setPageSize(50);

        ProfilesFilterDefaults.normalize(filter);

        assertEquals(50, filter.getPageSize());
    }

    @Test
    void normalize_shouldSetDefaultPage_whenPageIsLessThanOne() {
        ProfilesFilter filter = new ProfilesFilter();
        filter.setPage(0);

        ProfilesFilterDefaults.normalize(filter);

        assertEquals(1, filter.getPage());
    }

    @Test
    void normalize_shouldSetDefaultPageSize_whenPageSizeIsNotAllowed() {
        ProfilesFilter filter = new ProfilesFilter();
        filter.setPageSize(15);

        ProfilesFilterDefaults.normalize(filter);

        assertEquals(10, filter.getPageSize());
    }
}
