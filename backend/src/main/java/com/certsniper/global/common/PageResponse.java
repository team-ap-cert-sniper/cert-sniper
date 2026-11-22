package com.certsniper.global.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "목록 응답")
public record PageResponse<T>(
        @Schema(description = "항목 목록") List<T> items,
        @Schema(description = "전체 건수") long total,
        @Schema(description = "페이지 (0부터)") int page,
        @Schema(description = "페이지 크기") int size
) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getTotalElements(), page.getNumber(), page.getSize());
    }
}
