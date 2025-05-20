package com.unipay.gateway;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.unipay.service.payment.request.PaymentRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


@Service
public class StripeGateway {


    public void charge(PaymentRequest request) {
        //Stripe.apiKey = stripeSecretKey;

        Map<String, Object> params = new HashMap<>();
        params.put("amount", request.amount().multiply(BigDecimal.valueOf(100)).intValue());
        params.put("currency", request.currency());
        params.put("source", request.paymentDetails().get("token"));

        try {
            Charge charge = Charge.create(params);
            /*return new GatewayResponse(
                    charge.getId(),
                    charge.getStatus(),
                    charge.toJson()
            );*/
        } catch (StripeException e) {
            //throw new PaymentException("Stripe charge failed: " + e.getMessage());
        }
    }
}
