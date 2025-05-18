package com.unipay.response;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailConfirmationResponseFailed extends EmailConfirmationResponse{
    public EmailConfirmationResponseFailed(String code, String message) {
        super("ERROR", code, message);
    }
}
