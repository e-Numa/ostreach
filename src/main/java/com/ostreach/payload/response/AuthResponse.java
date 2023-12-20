package com.ostreach.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import static com.ostreach.utils.DateUtils.toDateString;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String profilePic;
    private String dateTime;

    public AuthResponse(String accessToken, String refreshToken, String profilePic) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.profilePic = profilePic;
        this.dateTime = toDateString(LocalDateTime.now());
    }
}
