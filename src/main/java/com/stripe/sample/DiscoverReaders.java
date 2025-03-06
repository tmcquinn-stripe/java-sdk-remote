package com.stripe.sample;

import com.stripe.stripeterminal.Terminal;
import com.stripe.stripeterminal.external.callable.Callback;
import com.stripe.stripeterminal.external.callable.Cancelable;
import com.stripe.stripeterminal.external.callable.DiscoveryListener;
import com.stripe.stripeterminal.external.models.DiscoveryConfiguration.UsbDiscoveryConfiguration;
import com.stripe.stripeterminal.external.models.Reader;
import com.stripe.stripeterminal.external.models.TerminalException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DiscoverReaders implements DiscoveryListener {

    Cancelable discoverCancelable = null;
    public static List<Reader> readersList = null;

    @Override
    public void onUpdateDiscoveredReaders(@NotNull List<Reader> readers) {
        readersList = readers;

        // In your app, display the discovered reader(s) to the user.
        // Call `connectReader` after the user selects a reader to connect to.
        // IDK what this is doing to be honest.
    }

    public static List<Reader> getReaderList() {
        return readersList;
    }
}
