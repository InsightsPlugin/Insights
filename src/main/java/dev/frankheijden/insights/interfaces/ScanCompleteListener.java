package dev.frankheijden.insights.interfaces;

import dev.frankheijden.insights.events.ScanCompleteEvent;

/**
 * Listener to listen for when a scan completes.
 */
public interface ScanCompleteListener {

    /**
     * Called when the scan has been completed.
     * @param event The event
     */
    void onScanComplete(ScanCompleteEvent event);
}
