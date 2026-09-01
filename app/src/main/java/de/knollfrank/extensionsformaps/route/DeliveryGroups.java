package de.knollfrank.extensionsformaps.route;

import java.util.List;
import java.util.stream.IntStream;

public class DeliveryGroups {

    // FK-TODO: replace magic number 10
    public static final List<DeliveryGroup> DELIVERY_GROUPS = createDeliveryGroups(10);

    private static List<DeliveryGroup> createDeliveryGroups(final int num) {
        return DeliveryGroups
                .createSequenceOrders(num)
                .mapToObj(DeliveryGroups::createDeliveryGroup)
                .toList();
    }

    private static IntStream createSequenceOrders(final int num) {
        final int startSequenceOrder = 1;
        return IntStream.range(startSequenceOrder, num + startSequenceOrder);
    }

    private static DeliveryGroup createDeliveryGroup(final int sequenceOrder) {
        return new DeliveryGroup("" + sequenceOrder, sequenceOrder);
    }
}
