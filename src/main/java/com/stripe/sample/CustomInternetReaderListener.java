package com.stripe.sample;

import com.stripe.stripeterminal.external.callable.InternetReaderListener;
import com.stripe.stripeterminal.external.models.DisconnectReason;
import org.jetbrains.annotations.NotNull;

public class CustomInternetReaderListener implements InternetReaderListener {
    @Override
    public void onDisconnect(@NotNull DisconnectReason reason) {
        System.out.println("!!! Reader Disconnected due to " + reason.toString());
    }
}
