package ru.andrewb.charm.back.profiles;

import lombok.experimental.UtilityClass;
import ru.andrewb.charm.back.dto.ProfileFilter;
import ru.andrewb.charm.back.dto.sort.SortBy;
import ru.andrewb.charm.back.dto.sort.SortOrder;

import java.util.List;

@UtilityClass
public class ProfileDefaults {
    public static final SortBy DEFAULT_SORT_BY = SortBy.ID;
    public static final SortOrder DEFAULT_SORT_ORDER = SortOrder.ASC;
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final List<Integer> AVAILABLE_PAGE_SIZES = List.of(10, 20, 50, 100);

    public static ProfileFilter normalize(ProfileFilter f) {
        if (f.getSortBy() == null) f.setSortBy(DEFAULT_SORT_BY);
        if (f.getSortOrder() == null) f.setSortOrder(DEFAULT_SORT_ORDER);

        Integer page = f.getPage();
        f.setPage(page == null || page < 1 ? DEFAULT_PAGE : page);

        Integer ps = f.getPageSize();
        if (ps == null || !AVAILABLE_PAGE_SIZES.contains(ps)) {
            f.setPageSize(DEFAULT_PAGE_SIZE);
        }
        return f;
    }
}