package de.knollfrank.extensionsformaps.route.protobuf;

public enum DirectionsVisibility {

    HIDDEN("0"),
    VISIBLE("1");

    public final String value;

    DirectionsVisibility(final String value) {
        this.value = value;
    }
}
