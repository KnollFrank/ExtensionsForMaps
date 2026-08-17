package de.knollfrank.extensionsformaps.route;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.WireFormat;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

public class GeocodeTokenParser {

    public static class GeocodeData {

        public Optional<Double> latitude = Optional.empty();
        public Optional<Double> longitude = Optional.empty();
        public List<String> extractedStrings = new ArrayList<>();
        public Optional<String> officialPlaceId = Optional.empty();
    }

    private static class ParserState {

        public OptionalLong id1 = OptionalLong.empty();
        public OptionalLong id2 = OptionalLong.empty();
    }

    public static Optional<GeocodeData> parseToken(final String token) {
        try {
            return Optional.of(parse(getCodedInputStream(token)));
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    private static GeocodeData parse(final CodedInputStream input) throws Exception {
        final GeocodeData data = new GeocodeData();
        final ParserState state = new ParserState();
        parseProtobufStream(input, data, state);
        state.id1.ifPresent(
                id1 ->
                        state.id2.ifPresent(
                                id2 ->
                                        data.officialPlaceId =
                                                Optional.of(
                                                        GeocodeTokenParser
                                                                .getUndocumentedPlaceId(id1, id2)
                                                                .toOfficialPlaceId()
                                                                .value())));
        return data;
    }

    private static UndocumentedPlaceId getUndocumentedPlaceId(final long id1, final long id2) {
        return new UndocumentedPlaceId(String.format("0x%x:0x%x", id1, id2));
    }

    private static void parseProtobufStream(final CodedInputStream input,
                                            final GeocodeData data,
                                            final ParserState state) throws Exception {
        while (!input.isAtEnd()) {
            final int tag = input.readTag();
            if (tag == 0) {
                break;
            }

            final int fieldNumber = WireFormat.getTagFieldNumber(tag);
            final int wireType = WireFormat.getTagWireType(tag);

            switch (wireType) {
                case WireFormat.WIRETYPE_VARINT:
                    final long varintVal = input.readInt64();
                    checkAndAssignCoordinate(varintVal / 1e7, data);
                    break;
                case WireFormat.WIRETYPE_LENGTH_DELIMITED:
                    final byte[] bytes = input.readBytes().toByteArray();
                    if (!tryParseAsSubMessage(bytes, data, state)) {
                        final String text = new String(bytes, java.nio.charset.StandardCharsets.UTF_8).trim();
                        if (text.length() >= 3 && text.matches("^[\\w\\-.:/]+$")) {
                            data.extractedStrings.add(text);
                            if (text.startsWith("0x") && text.contains(":")) {
                                data.officialPlaceId = Optional.of(text);
                            }
                        }
                    }
                    break;
                case WireFormat.WIRETYPE_FIXED32:
                    final int fixed32 = input.readFixed32();
                    if (fieldNumber == 2 || fieldNumber == 3) {
                        checkAndAssignCoordinate(fixed32 / 1e6, data);
                    } else {
                        checkAndAssignCoordinate(fixed32 / 1e7, data);
                    }
                    break;
                case WireFormat.WIRETYPE_FIXED64:
                    final long fixed64 = input.readFixed64();
                    if (fieldNumber == 5) {
                        state.id1 = OptionalLong.of(fixed64);
                    } else if (fieldNumber == 6) {
                        state.id2 = OptionalLong.of(fixed64);
                    }
                    checkAndAssignCoordinate(Double.longBitsToDouble(fixed64), data);
                    break;
                default:
                    input.skipField(tag);
                    break;
            }
        }
    }

    private static boolean tryParseAsSubMessage(final byte[] bytes,
                                                final GeocodeData data,
                                                final ParserState state) {
        try {
            final CodedInputStream subInput = CodedInputStream.newInstance(bytes);
            parseProtobufStream(subInput, data, state);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    private static void checkAndAssignCoordinate(final double val, final GeocodeData data) {
        if (val >= -90.0 && val <= 90.0 && data.latitude.isEmpty() && val != 0.0) {
            data.latitude = Optional.of(val);
        } else if (val >= -180.0 && val <= 180.0 && data.longitude.isEmpty() && val != 0.0) {
            data.longitude = Optional.of(val);
        }
    }

    private static CodedInputStream getCodedInputStream(final String token) {
        return CodedInputStream.newInstance(decode(getCleanToken(token)));
    }

    private static String getCleanToken(final String token) {
        return token
                .replace("-", "+")
                .replace("_", "/");
    }

    private static byte[] decode(final String base64Encoded) {
        return Base64.getDecoder().decode(base64Encoded);
    }
}
