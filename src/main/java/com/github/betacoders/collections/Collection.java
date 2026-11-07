package com.github.betacoders.collections;

import java.util.Objects;

public interface Collection<E> extends Iterable<E> {
  int size();

  default boolean isEmpty() {
    return size() == 0;
  }

  default boolean add(E element) {
    throw new UnsupportedOperationException("Unmodifiable collection");
  }

  default boolean remove(E element) {
    throw new UnsupportedOperationException("Unmodifiable collection");
  }

  default boolean contains(E element) {
    for (E item : this)
      if (Objects.equals(item, element))
        return true;
    return false;
  }

  default boolean addAll(Collection<? extends E> c) {
    boolean modified = false;
    for (E element : c)
      if (add(element))
        modified = true;
    return modified;
  }

  default void clear() {
    throw new UnsupportedOperationException("Unmodifiable collection");
  }
}
