package com.ostreach.payload.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DescriptionResponse {
    private String itemDescription;
    private String pickUpLocation;
    private String dropOffLocation;
}
