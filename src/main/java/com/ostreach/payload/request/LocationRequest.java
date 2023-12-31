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
public class LocationRequest {
    @Size(min = 3, max = 255, message = "Location too short or long")
    @NotBlank(message = "Pick-Up location can not be empty")
    private String pickUpLocation;

    @Size(min = 3, max = 255, message = "Location too short or long")
    @NotBlank(message = "Drop-Off location can not be empty")
    private String dropOffLocation;
}