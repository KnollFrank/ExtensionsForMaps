package de.KnollFrank.routeoptimizerforgooglemaps.route.protobuf;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class NodeParser {

    private static final String FIELD_ID = "fieldId";
    private static final String DATA_TYPE = "dataType";
    private static final String VALUE = "value";
    private static final Pattern TOKEN_PATTERN =
            Pattern.compile(
                    String.format(
                            "^(?<%s>\\d+)(?<%s>.)(?<%s>.*)$",
                            FIELD_ID, DATA_TYPE, VALUE));

    public static Node parseNode(final String token) {
        final Matcher matcher = TOKEN_PATTERN.matcher(token);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(token);
        }
        return new Node(
                getFieldId(matcher),
                getDatatype(matcher),
                getValue(matcher),
                List.of());
    }

    private static int getFieldId(final Matcher matcher) {
        return Integer.parseInt(getGroup(matcher, FIELD_ID));
    }

    private static Datatype getDatatype(final Matcher matcher) {
        return new Datatype(
                NodeParser
                        .getGroup(matcher, DATA_TYPE)
                        .charAt(0));
    }

    private static String getValue(final Matcher matcher) {
        return getGroup(matcher, VALUE);
    }

    private static String getGroup(final Matcher matcher, final String group) {
        return Objects.requireNonNull(matcher.group(group));
    }
}
