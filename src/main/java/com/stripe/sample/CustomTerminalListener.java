package com.stripe.sample;

import com.stripe.stripeterminal.external.callable.TerminalListener;
import com.stripe.stripeterminal.external.models.ConnectionStatus;
import com.stripe.stripeterminal.external.models.PaymentStatus;

public class CustomTerminalListener implements TerminalListener {
    @Override
    public void onConnectionStatusChange(ConnectionStatus status) {
        System.out.printf("onConnectionStatusChange: %s\n", status);
    }

    @Override
    public void onPaymentStatusChange(PaymentStatus status) {
        System.out.printf("onPaymentStatusChange: %s\n ", status);
    }
}
