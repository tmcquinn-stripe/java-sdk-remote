package com.stripe.sample;

import com.stripe.stripeterminal.external.callable.OfflineListener;
import com.stripe.stripeterminal.external.models.OfflineStatus;
import com.stripe.stripeterminal.external.models.PaymentIntent;
import com.stripe.stripeterminal.external.models.TerminalException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomOfflineListener implements OfflineListener {

    @Override
    public void onForwardingFailure(@NotNull TerminalException e) {
        System.out.println("FOWARDING FAILED");
    }

    @Override
    public void onOfflineStatusChange(@NotNull OfflineStatus offlineStatus) {
        System.out.println("offlineStatus Changed");

    }

    @Override
    public void onPaymentIntentForwarded(@NotNull PaymentIntent paymentIntent, @Nullable TerminalException e) {
        System.out.println("PI Forwarded FAILED");

    }
}
