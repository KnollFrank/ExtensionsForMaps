package de.knollfrank.extensionsformaps.common;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

public class Maps {

    private Maps() {
    }

    public static <K, V> ImmutableMap<K, V> merge(final Collection<Map<K, V>> maps) {
        return Maps
                .getEntryStream(maps)
                .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
    }

    // adapted from https://stackoverflow.com/a/31954986
    public static <K, V> ImmutableBiMap<K, V> mergeBiMaps(final Collection<BiMap<K, V>> biMaps) {
        return Maps
                .getEntryStream(biMaps)
                .collect(ImmutableBiMap.toImmutableBiMap(Entry::getKey, Entry::getValue));
    }

    public static <K, V> Optional<V> get(final Map<K, V> map, final K key) {
        return Optional.ofNullable(map.get(key));
    }

    public static <K, V> List<V> getAll(final Map<K, V> map, final List<K> keys) {
        return keys
                .stream()
                .map(map::get)
                .toList();
    }

    public static <K, V> ImmutableMap<K, V> filterPresentValues(final Map<K, Optional<V>> map) {
        return map
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().isPresent())
                .collect(
                        ImmutableMap.toImmutableMap(
                                Entry::getKey,
                                entry -> entry.getValue().orElseThrow()));
    }

    public static <Key, Value, ValueMapped> ImmutableMap<Key, ValueMapped> mapValues(
            final Map<Key, Value> map,
            final Function<Value, ValueMapped> valueMapper) {
        return mapKeysAndValues(map, Function.identity(), valueMapper);
    }

    public static <K, V> ImmutableMap<K, V> mapEachKeyToValue(final Set<K> keys, final V value) {
        return keys
                .stream()
                .collect(
                        ImmutableMap.toImmutableMap(
                                Function.identity(),
                                key -> value));
    }

    public static <K, V, KMapped, VMapped> ImmutableMap<KMapped, VMapped> mapKeysAndValues(
            final Map<K, V> map,
            final Function<K, KMapped> keyMapper,
            final Function<V, VMapped> valueMapper) {
        return map
                .entrySet()
                .stream()
                .collect(
                        ImmutableMap.toImmutableMap(
                                entry -> keyMapper.apply(entry.getKey()),
                                entry -> valueMapper.apply(entry.getValue())));
    }

    public static <K, V> ImmutableMap<K, V> filter(final Map<K, V> map, final BiPredicate<K, V> predicate) {
        return map
                .entrySet()
                .stream()
                .filter(entry -> predicate.test(entry.getKey(), entry.getValue()))
                .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
    }

    private static <K, V> Stream<Entry<K, V>> getEntryStream(final Collection<? extends Map<K, V>> maps) {
        return maps
                .stream()
                .map(Map::entrySet)
                .flatMap(Set::stream);
    }
}
