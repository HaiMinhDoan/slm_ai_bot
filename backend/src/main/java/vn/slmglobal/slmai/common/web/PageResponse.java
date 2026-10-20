package vn.slmglobal.slmai.common.web;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Khớp kiểu {@code BasicFetchResult<T>} của Vben ({@code items}, {@code total}) để BasicTable dùng được ngay. */
public record PageResponse<T>(List<T> items, long total, int page, int size, int totalPages) {

    public static <E, D> PageResponse<D> of(Page<E> source, Function<E, D> mapper) {
        return new PageResponse<>(
                source.getContent().stream().map(mapper).toList(),
                source.getTotalElements(),
                source.getNumber(),
                source.getSize(),
                source.getTotalPages());
    }
}
