package com.github.betacoders.collections;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Map<K, V> {
  int size();

  V get(K key);

  void clear();

  Set<K> keySet();

  Collection<V> values();

  Set<Entry<K, V>> entrySet();

  default V put(K key, V value) {
    throw new UnsupportedOperationException("Read-only map");
  }

  default V remove(K key) {
    throw new UnsupportedOperationException("Read-only map");
  }

  default V compute(K key, BiFunction<? super K, ? super V, ? extends V> remap) {
    V oldValue = this.get(key);
    V newValue = remap.apply(key, oldValue);

    if (oldValue != null)
      if (newValue != null)
        this.put(key, newValue);
      else
        this.remove(key);
    else if (newValue != null)
      this.put(key, newValue);
    else
      return null;

    return newValue;
  }

  default V computeIfAbsent(K key, Function<? super K, ? extends V> fmap) {
    if (this.get(key) == null) {
      V newValue = fmap.apply(key);
      if (newValue != null)
        this.put(key, newValue);

      return newValue;
    }

    return null;
  }

  record Entry<K, V>(K key, V value) {
  }
}
