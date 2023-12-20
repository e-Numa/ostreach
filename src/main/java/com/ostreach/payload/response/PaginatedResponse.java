package com.ostreach.payload.response;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedResponse<T> {
    private List<T> content;
    private int pageNo;
    private int pageSize;
    private long totalElement;
    private boolean isLast;
}
