package net.frankheijden.insights.interfaces;

import net.frankheijden.insights.events.ScanCompleteEvent;

public interface ScanCompleteListener {

    void onScanComplete(ScanCompleteEvent event);
}
