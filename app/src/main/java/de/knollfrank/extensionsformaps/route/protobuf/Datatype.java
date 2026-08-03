package de.knollfrank.extensionsformaps.route.protobuf;

public record Datatype(char marker) {

    public static final Datatype DOUBLE = new Datatype('d');
    public static final Datatype FLOAT = new Datatype('f');
    public static final Datatype INTEGER = new Datatype('i');
    public static final Datatype STRING = new Datatype('s');
    public static final Datatype BOOLEAN = new Datatype('b');
    public static final Datatype CONTAINER = new Datatype('m');
}
