package com.stripe.sample;

import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.callable.MobileReaderListener;
import com.stripe.stripeterminal.external.models.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomMobileReaderListener implements MobileReaderListener {
    @Override
    public void onStartInstallingUpdate(@NotNull ReaderSoftwareUpdate update, @NotNull Cancelable cancelable) {
        // Show UI communicating that a required update has started installing
        System.out.println("Installing Update start");
    }

    @Override
    public void onReportReaderSoftwareUpdateProgress(float progress) {
        // Update the progress of the installation
        System.out.println("Installing update - progress: " + progress);
    }

    @Override
    public void onFinishInstallingUpdate(@Nullable ReaderSoftwareUpdate update, @Nullable TerminalException e) {
        // Report success or failure of the update
        System.out.println("Finished Update");

    }
    @Override
    public void onRequestReaderInput(ReaderInputOptions options) {
        // Placeholder for updating your app's checkout UI
    }

    @Override
    public void onRequestReaderDisplayMessage(ReaderDisplayMessage message) {
    }

    @Override
    public void onDisconnect(@NotNull DisconnectReason reason) {
        System.out.println("TESTING ONDISCONNECT");

        // Consider displaying a UI to notify the user and start rediscovering readers
    }

    @Override
    public void onReaderReconnectStarted(@NotNull Reader reader, @NotNull Cancelable cancelReconnect, @NotNull DisconnectReason reason) {
        System.out.println("TESTING ON RECONNECT STARTED");
        // 1. Notified at the start of a reconnection attempt
        // Use cancelable to stop reconnection at any time
    }

    @Override
    public void onReaderReconnectSucceeded(@NotNull Reader reader) {
        // 2. Notified when reader reconnection succeeds
        // App is now connected
    }

    @Override
    public void onReaderReconnectFailed(@NotNull Reader reader) {
        // 3. Notified when reader reconnection fails
        // App is now disconnected
    }

}
