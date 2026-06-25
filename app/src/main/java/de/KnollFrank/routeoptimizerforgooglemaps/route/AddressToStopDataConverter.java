package de.KnollFrank.routeoptimizerforgooglemaps.route;

import java.util.List;
import java.util.Optional;

import de.KnollFrank.routeoptimizerforgooglemaps.common.Lists;

class AddressToStopDataConverter {

    public static List<StopData> convert(final List<String> addresses) {
        return Lists
                .asIndexedElements(addresses)
                .stream()
                .map(indexedAddress -> convert(indexedAddress.element(), indexedAddress.index()))
                .toList();
    }

    private static StopData convert(final String address, final int stopNumber) {
        final StopData stopData = new StopData(address, stopNumber);
        if (address.matches("-?\\d+\\.\\d+,-?\\d+\\.\\d+")) {
            final String[] coords = address.split(",");
            stopData.latitude = Optional.of(Double.parseDouble(coords[0]));
            stopData.longitude = Optional.of(Double.parseDouble(coords[1]));
        }
        return stopData;
    }
}
