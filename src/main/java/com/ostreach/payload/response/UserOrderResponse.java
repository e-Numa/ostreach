package com.ostreach.payload.response;

import com.ostreach.entities.enums.OrderStatus;
import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserOrderResponse {
    private Long orderId;
    private String receiver;
    private String dropOffLocation;
    private String pickUpLocation;
    private String sender;
    private String date;
    private OrderStatus status;
    private String amount;
    private String itemName;
    private String trackingNum;
    private String imageURL;
    private List<TrackingResponse> locationList;
}
