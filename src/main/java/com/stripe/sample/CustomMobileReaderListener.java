package com.stripe.sample;

import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.callable.MobileReaderListener;
import com.stripe.stripeterminal.external.models.ReaderDisplayMessage;
import com.stripe.stripeterminal.external.models.ReaderInputOptions;
import com.stripe.stripeterminal.external.models.ReaderSoftwareUpdate;
import com.stripe.stripeterminal.external.models.TerminalException;
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
}
