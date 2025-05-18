package com.unipay.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class EmailConfirmationResponse {
    protected String status;
    protected String code;
    protected String message;
}
