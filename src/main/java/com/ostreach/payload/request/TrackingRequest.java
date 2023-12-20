package com.ostreach.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrackingRequest {
    @Size(min = 3, max = 255, message = "Location too short or long!")
    @NotBlank(message = "Location cannot be empty!")
    private String location;
}