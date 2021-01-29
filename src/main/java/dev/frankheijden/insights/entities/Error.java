package dev.frankheijden.insights.entities;

public abstract class Error {

    private final String error;

    public Error(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    @Override
    public String toString() {
        return " &c" + error;
    }
}
