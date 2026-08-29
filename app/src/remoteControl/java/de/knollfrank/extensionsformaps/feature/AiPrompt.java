package de.knollfrank.extensionsformaps.feature;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AiPrompt {

    private static final String tokenStart = "START_ADDR";
    private static final String tokenEnd = "END_ADDR";
    public static final String aiPrompt = "Analysiere das Bild und extrahiere nur die Adresse (ohne Namen). Antwort-Format: " + tokenStart + " [gefundene Adresse hier einsetzen] " + tokenEnd;
    private static final Pattern aiAnswerPattern =
            Pattern.compile(
                    tokenStart + "\\s*[*]*\\s*(.*?)\\s*[*]*\\s*" + tokenEnd,
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    public static Optional<String> getAddress(final String aiAnswer) {
        final Matcher matcher = aiAnswerPattern.matcher(aiAnswer);
        while (matcher.find()) {
            final String candidate = matcher.group(1).trim();
            if (isValidAddress(candidate)) {
                return Optional.of(
                        candidate
                                .replaceAll("[\\r\\n]+", " ")
                                .replaceAll("\\s{2,}", " ")
                                .trim());
            }
        }
        return Optional.empty();
    }

    private static boolean isValidAddress(String text) {
        if (text.length() < 5) {
            return false;
        }
        final String lower = text.toLowerCase();
        return !lower.contains("gefundene adresse") && !lower.contains("extrahiere") && text.matches(".*\\d+.*");
    }
}
