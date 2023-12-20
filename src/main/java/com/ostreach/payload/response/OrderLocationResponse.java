package com.ostreach.payload.response;

import lombok.*;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderLocationResponse {
    private Long orderId;
    private String pickUpLocation;
    private String dropOffLocation;
}