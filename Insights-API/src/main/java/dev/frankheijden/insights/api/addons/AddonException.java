package dev.frankheijden.insights.api.addons;

import java.io.IOException;

public class AddonException extends IOException {

    public AddonException(String msg) {
        super(msg);
    }

    public AddonException(Throwable cause) {
        super(cause);
    }
}
