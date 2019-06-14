package net.frankheijden.insights.api.interfaces;

import net.frankheijden.insights.api.events.ScanCompleteEvent;

public interface ScanCompleteEventListener {
    void onScanComplete(ScanCompleteEvent event);
}
