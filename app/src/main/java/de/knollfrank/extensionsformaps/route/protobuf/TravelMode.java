package de.knollfrank.extensionsformaps.route.protobuf;

public enum TravelMode {

    BICYCLING("1"),
    WALKING("2"),
    DRIVING("3"),
    TRANSIT("4");

    public final String value;

    TravelMode(final String value) {
        this.value = value;
    }
}
