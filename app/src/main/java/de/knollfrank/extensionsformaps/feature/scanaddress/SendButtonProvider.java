package de.knollfrank.extensionsformaps.feature.scanaddress;

import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Optional;

import de.knollfrank.extensionsformaps.accessibility.GoogleAppContext;
import de.knollfrank.extensionsformaps.accessibility.ResourceName;
import de.knollfrank.extensionsformaps.accessibility.ResourceNameFactory;
import de.knollfrank.extensionsformaps.accessibility.wrapper.AccessibilityNodeInfoWrapper;

class SendButtonProvider {

    private static final ResourceName AIM_SEND_BUTTON_ID = ResourceNameFactory.createGoogleAppResourceName("searchbox_aim_enter_button");

    private final GoogleAppContext googleAppContext;

    public SendButtonProvider(final GoogleAppContext googleAppContext) {
        this.googleAppContext = googleAppContext;
    }

    public Optional<AccessibilityNodeInfo> findSendButton(final AccessibilityNodeInfo root) {
        final AccessibilityNodeInfoWrapper wrapper = new AccessibilityNodeInfoWrapper(root);

        // 1. Primäre Suche nach Resource-ID
        final Optional<AccessibilityNodeInfo> byId = wrapper.findFirstAccessibilityNodeInfoByViewId(AIM_SEND_BUTTON_ID);
        if (byId.isPresent() && byId.get().isEnabled()) {
            return byId;
        }

        // 2. Sekundäre Suche nach Content-Description / Text der Google App ("Send")
        return wrapper
                .streamPreOrder()
                .filter(node -> {
                    final AccessibilityNodeInfoWrapper nodeWrapper = new AccessibilityNodeInfoWrapper(node);
                    final String contentDesc = nodeWrapper.getContentDescription().orElse("");
                    final String text = nodeWrapper.getText().orElse("");
                    final String viewId = node.getViewIdResourceName();
                    final boolean matches = (viewId != null && viewId.contains("aim_enter_button"))
                            || contentDesc.equalsIgnoreCase(googleAppContext.sendText())
                            || text.equalsIgnoreCase(googleAppContext.sendText());
                    return matches && node.isEnabled();
                })
                .findFirst();
    }
}
