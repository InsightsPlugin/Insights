package net.frankheijden.insights.api.events;

import net.frankheijden.insights.api.entities.ScanOptions;
import net.frankheijden.insights.api.entities.ScanResult;

public class ScanCompleteEvent {
    private final ScanOptions scanOptions;
    private final ScanResult scanResult;

    /**
     * Event which is called when an Insights Scan™ has been completed.
     * Event is retrieved using the ScanCompleteEventListener.
     *
     * @param scanOptions ScanOptions which were used to scan.
     * @param scanResult Result of the Scan with ScanOptions.
     */
    public ScanCompleteEvent(ScanOptions scanOptions, ScanResult scanResult) {
        this.scanOptions = scanOptions;
        this.scanResult = scanResult;
    }

    /**
     * Retrieves the scanresults
     *
     * @return ScanResult
     */
    public ScanResult getScanResult() {
        return scanResult;
    }

    /**
     * Retrieves the scanoptions which were used
     * for scanning.
     *
     * @return ScanResult
     */
    public ScanOptions getScanOptions() {
        return scanOptions;
    }
}
