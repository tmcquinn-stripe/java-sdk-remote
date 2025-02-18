package com.stripe.sample;

import com.stripe.Stripe;
import com.stripe.model.terminal.ConnectionToken;
import com.stripe.param.terminal.ConnectionTokenCreateParams;
import com.stripe.stripeterminal.external.callable.ConnectionTokenCallback;
import com.stripe.stripeterminal.external.callable.ConnectionTokenProvider;
import com.stripe.stripeterminal.external.models.ConnectionTokenException;

public class CustomConnectionTokenProvider implements ConnectionTokenProvider {
    @Override
    public void fetchConnectionToken(ConnectionTokenCallback callback) {
        try {
            Stripe.apiKey = "{SECRET API KEY}";

            ConnectionTokenCreateParams ctp = ConnectionTokenCreateParams.builder().build();
            ConnectionToken connectionToken = ConnectionToken.create(ctp);

            callback.onSuccess(connectionToken.getSecret());
        } catch (Exception e) {
            callback.onFailure(
                    new ConnectionTokenException("Failed to fetch connection token", e)
            );
        }
    }
}
