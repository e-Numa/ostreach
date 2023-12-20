package com.ostreach.payload.response;

import com.ostreach.entities.enums.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse  {
    private Long orderId;
    private OrderStatus status;
    private LocalDateTime time;
}