package com.github.betacoders.collections;

/**
 * List
 */
public interface List<T> extends Iterable<T> {
  T get(int i);

  void set(int i, T val);

  boolean isEmpty();

  boolean add(T item);

  boolean addAll(List<T> list);

  void remove(T item);

  T remove(int i);

  void insert(int i, T item);

  int size();
}
