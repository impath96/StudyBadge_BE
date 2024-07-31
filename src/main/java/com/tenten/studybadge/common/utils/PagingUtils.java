package com.tenten.studybadge.common.utils;

import com.tenten.studybadge.type.study.channel.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PagingUtils {

    private PagingUtils() {}

    public static Pageable createPageable(int page, int size, SortOrder sortOrder) {
        page = (page <= 0) ? 0 : (page - 1);
        return PageRequest.of(page, size,  createSort(sortOrder));
    }

    // TODO 정렬 기준 추가되면 어떻게 할지 고민
    private static Sort createSort(SortOrder sortOrder) {
        if (sortOrder.equals(SortOrder.VIEW_COUNT)) {
            return Sort.by(Sort.Direction.DESC, "viewCnt");
        }
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }

}
