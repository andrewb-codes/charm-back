package ru.andrewb.charm.back.normalizer;

import org.junit.jupiter.api.Test;
import ru.andrewb.charm.back.dto.ProfileFilter;
import ru.andrewb.charm.back.dto.sort.SortBy;
import ru.andrewb.charm.back.dto.sort.SortOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ProfileFilterDefaultsTest {

    @Test
    void normalize_shouldApplyAllDefaults_whenFieldsAreNull() {
        ProfileFilter filter = new ProfileFilter();

        ProfileFilter result = ProfileFilterDefaults.normalize(filter);

        assertSame(filter, result);
        assertEquals(SortBy.ID, filter.getSortBy());
        assertEquals(SortOrder.ASC, filter.getSortOrder());
        assertEquals(1, filter.getPage());
        assertEquals(10, filter.getPageSize());
    }

    @Test
    void normalize_shouldKeepExplicitSortSettings() {
        ProfileFilter filter = new ProfileFilter();
        filter.setSortBy(SortBy.EMAIL);
        filter.setSortOrder(SortOrder.DESC);
        filter.setPage(2);
        filter.setPageSize(20);

        ProfileFilterDefaults.normalize(filter);

        assertEquals(SortBy.EMAIL, filter.getSortBy());
        assertEquals(SortOrder.DESC, filter.getSortOrder());
        assertEquals(2, filter.getPage());
        assertEquals(20, filter.getPageSize());
    }

    @Test
    void normalize_shouldSetDefaultPage_whenPageIsNull() {
        ProfileFilter filter = new ProfileFilter();
        filter.setPage(null);

        ProfileFilterDefaults.normalize(filter);

        assertEquals(1, filter.getPage());
    }

    @Test
    void normalize_shouldSetDefaultPageSize_whenPageSizeIsNull() {
        ProfileFilter filter = new ProfileFilter();
        filter.setPageSize(null);

        ProfileFilterDefaults.normalize(filter);

        assertEquals(10, filter.getPageSize());
    }

    @Test
    void normalize_shouldKeepPageSize_whenItIsAllowed() {
        ProfileFilter filter = new ProfileFilter();
        filter.setPageSize(50);

        ProfileFilterDefaults.normalize(filter);

        assertEquals(50, filter.getPageSize());
    }

    @Test
    void normalize_shouldSetDefaultPage_whenPageIsLessThanOne() {
        ProfileFilter filter = new ProfileFilter();
        filter.setPage(0);

        ProfileFilterDefaults.normalize(filter);

        assertEquals(1, filter.getPage());
    }

    @Test
    void normalize_shouldSetDefaultPageSize_whenPageSizeIsNotAllowed() {
        ProfileFilter filter = new ProfileFilter();
        filter.setPageSize(15);

        ProfileFilterDefaults.normalize(filter);

        assertEquals(10, filter.getPageSize());
    }
}
