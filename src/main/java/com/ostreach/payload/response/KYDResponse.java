package com.ostreach.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KYDResponse {
    private Long kydId;
    private String kydUrl;
    private String idCardNumber;
}