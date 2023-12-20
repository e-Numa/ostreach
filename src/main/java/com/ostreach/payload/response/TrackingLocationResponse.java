package com.ostreach.payload.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrackingLocationResponse {
    private Long locationId;
    private String location;
    private String dateTime;
}