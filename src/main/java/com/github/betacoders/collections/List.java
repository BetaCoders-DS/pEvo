package com.github.betacoders.collections;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public interface List<T> extends Iterable<T> {
  static <T> List<T> of() {
    return (List<T>) Empty.INSTANCE;
  }

  int size();

  T get(int i);

  default boolean isEmpty() {
    return size() == 0;
  }

  default T set(int i, T val) {
    throw new UnsupportedOperationException("Unmodifiable list");
  }

  default boolean add(T item) {
    throw new UnsupportedOperationException("Unmodifiable list");
  }

  default void add(int i, T item) {
    throw new UnsupportedOperationException("Unmodifiable list");
  }

  default boolean addAll(List<T> list) {
    throw new UnsupportedOperationException("Unmodifiable list");
  }

  default boolean remove(T item) {
    throw new UnsupportedOperationException("Unmodifiable list");
  }

  default T remove(int i) {
    throw new UnsupportedOperationException("Unmodifiable list");
  }

  default boolean contains(T item) {
    for (T element : this) {
      if (element.equals(item)) {
        return true;
      }
    }
    return false;
  }

  @Override
  default Iterator<T> iterator() {
    return new Iterator<>() {
      private int i = 0;

      @Override
      public boolean hasNext() {
        return i < size();
      }

      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return get(i++);
      }
    };
  }

  static final class Empty<E> implements List<E> {
    private static final Empty<?> INSTANCE = new Empty<>();

    @Override
    public int size() {
      return 0;
    }

    @Override
    public E get(int i) {
      throw new IndexOutOfBoundsException("Index " + i + " out of bounds for length 0");
    }

    @Override
    public Iterator<E> iterator() {
      return (Iterator<E>) Collections.emptyIterator();
    }
  }
}
