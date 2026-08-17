package de.knollfrank.extensionsformaps.route;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.WireFormat;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class GeocodeTokenParser {

    public static class GeocodeData {

        public Optional<Double> latitude = Optional.empty();
        public Optional<Double> longitude = Optional.empty();
        public List<String> extractedStrings = new ArrayList<>();
        public Optional<String> officialPlaceId = Optional.empty();
    }

    private static class ParserState {

        public Long id1 = null;
        public Long id2 = null;
    }

    public static Optional<GeocodeData> parseToken(final String token) {
        try {
            return parseCleanToken(getCleanToken(token));
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    private static String getCleanToken(final String token) {
        return token
                .replace("-", "+")
                .replace("_", "/");
    }

    private static Optional<GeocodeData> parseCleanToken(final String cleanToken) throws Exception {
        final byte[] decodedBytes = Base64.getDecoder().decode(cleanToken);
        final CodedInputStream input = CodedInputStream.newInstance(decodedBytes);
        final GeocodeData data = new GeocodeData();
        final ParserState state = new ParserState();
        parseProtobufStream(input, data, state);

        if (state.id1 != null && state.id2 != null) {
            // Return as Official Place ID (Base64 encoded Protobuf)
            final UndocumentedPlaceId undocumentedId =
                    new UndocumentedPlaceId(
                            String.format(
                                    "0x%x:0x%x",
                                    state.id1,
                                    state.id2));
            data.officialPlaceId = Optional.of(undocumentedId.toOfficialPlaceId().value());
        }

        return Optional.of(data);
    }

    private static void parseProtobufStream(CodedInputStream input, GeocodeData data, ParserState state) throws Exception {
        while (!input.isAtEnd()) {
            int tag = input.readTag();
            if (tag == 0) break;

            int fieldNumber = WireFormat.getTagFieldNumber(tag);
            int wireType = WireFormat.getTagWireType(tag);

            switch (wireType) {
                case WireFormat.WIRETYPE_VARINT:
                    long varintVal = input.readInt64();
                    checkAndAssignCoordinate(varintVal / 1e7, data);
                    break;

                case WireFormat.WIRETYPE_LENGTH_DELIMITED:
                    byte[] bytes = input.readBytes().toByteArray();
                    if (!tryParseAsSubMessage(bytes, data, state)) {
                        String text = new String(bytes, java.nio.charset.StandardCharsets.UTF_8).trim();
                        if (text.length() >= 3 && text.matches("^[\\w\\-.:/]+$")) {
                            data.extractedStrings.add(text);
                            if (text.startsWith("0x") && text.contains(":")) {
                                data.officialPlaceId = Optional.of(text);
                            }
                        }
                    }
                    break;

                case WireFormat.WIRETYPE_FIXED32:
                    int fixed32 = input.readFixed32();
                    if (fieldNumber == 2 || fieldNumber == 3) {
                        checkAndAssignCoordinate(fixed32 / 1e6, data);
                    } else {
                        checkAndAssignCoordinate(fixed32 / 1e7, data);
                    }
                    break;

                case WireFormat.WIRETYPE_FIXED64:
                    long fixed64 = input.readFixed64();
                    if (fieldNumber == 5) {
                        state.id1 = fixed64;
                    } else if (fieldNumber == 6) {
                        state.id2 = fixed64;
                    }
                    checkAndAssignCoordinate(Double.longBitsToDouble(fixed64), data);
                    break;

                default:
                    input.skipField(tag);
                    break;
            }
        }
    }

    private static boolean tryParseAsSubMessage(byte[] bytes, GeocodeData data, ParserState state) {
        try {
            CodedInputStream subInput = CodedInputStream.newInstance(bytes);
            parseProtobufStream(subInput, data, state);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void checkAndAssignCoordinate(double val, GeocodeData data) {
        if (val >= -90.0 && val <= 90.0 && data.latitude.isEmpty() && val != 0.0) {
            data.latitude = Optional.of(val);
        } else if (val >= -180.0 && val <= 180.0 && data.longitude.isEmpty() && val != 0.0) {
            data.longitude = Optional.of(val);
        }
    }
}
