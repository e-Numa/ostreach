package com.ostreach.payload.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrackingResponse {
    private Long locationId;
    private String location;
    private String dateTime;
}