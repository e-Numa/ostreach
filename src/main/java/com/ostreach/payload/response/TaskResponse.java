package com.ostreach.payload.response;

import com.ostreach.entities.enums.TaskStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskResponse {
    private Long id;
    private TaskStatus taskStatus;
    private List<OrderResponse> orderResponse;
}