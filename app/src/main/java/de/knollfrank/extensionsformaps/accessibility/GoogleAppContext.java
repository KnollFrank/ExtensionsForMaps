package de.knollfrank.extensionsformaps.accessibility;

public final class GoogleAppContext {

    public final String askAnythingText;
    public final String sendText;
    public final String aiModeText;
    public final String takePhotoText;

    public GoogleAppContext(final String askAnythingText,
                            final String sendText,
                            final String aiModeText,
                            final String takePhotoText) {
        this.askAnythingText = askAnythingText;
        this.sendText = sendText;
        this.aiModeText = aiModeText;
        this.takePhotoText = takePhotoText;
    }
}
