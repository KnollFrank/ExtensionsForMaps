package de.knollfrank.extensionsformaps.common;

import java.util.List;

public record HeadAndTail<T>(T head, List<T> tail) {
}
