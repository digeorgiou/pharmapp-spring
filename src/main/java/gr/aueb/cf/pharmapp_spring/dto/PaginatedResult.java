package gr.aueb.cf.pharmapp_spring.dto;

import java.util.List;

public record PaginatedResult<T>(
        List<T> data,
        int currentPage,
        int pageSize,
        int totalPages,
        long totalItems
) {
}
