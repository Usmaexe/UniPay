package com.unipay.response;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailConfirmationResponseSuccess extends EmailConfirmationResponse{
    public EmailConfirmationResponseSuccess(String code, String message) {
        super("SUCCESS", code, message);
    }
}
