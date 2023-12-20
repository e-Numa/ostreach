package com.ostreach.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {

    @Size(max = 1000, message = "Receipt too long")
    @NotBlank(message = "Receipt cannot be empty!")
    private String receipt;

    @NotNull(message = "Amount cannot be empty!!!")
    private Double amount;
}