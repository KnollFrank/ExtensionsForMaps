package de.knollfrank.extensionsformaps.feature;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knollfrank.extensionsformaps.common.RegexUtils;

class AIPrompt {

    private static final String tokenStart = "START_ADDR";
    private static final String tokenEnd = "END_ADDR";

    public static String getAIPrompt() {
        return "Analysiere das Bild und extrahiere nur die Adresse (ohne Namen). Antwort-Format: " + tokenStart + " [gefundene Adresse hier einsetzen] " + tokenEnd;
    }

    public static Optional<String> extractAddressFromAIResponse(final String aiResponse) {
        return extractAddress(getAIResponsePattern().matcher(aiResponse));
    }

    private static Pattern getAIResponsePattern() {
        return Pattern.compile(
                tokenStart + "\\s*[*]*\\s*(.*?)\\s*[*]*\\s*" + tokenEnd,
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    }

    private static Optional<String> extractAddress(final Matcher matcher) {
        return RegexUtils
                .toStream(matcher)
                .map(matchResult -> matchResult.group(1).trim())
                .filter(AIPrompt::isValidAddress)
                .map(AIPrompt::clean)
                .findFirst();
    }

    private static boolean isValidAddress(final String str) {
        if (str.length() < 5) {
            return false;
        }
        final String lower = str.toLowerCase();
        return !lower.contains("gefundene adresse") && !lower.contains("extrahiere") && str.matches(".*\\d+.*");
    }

    private static String clean(final String candidate) {
        return candidate
                .replaceAll("[\\r\\n]+", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }
}
