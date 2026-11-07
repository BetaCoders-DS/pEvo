package com.github.betacoders.collections;

import java.util.Arrays;
import java.util.Objects;

public class Vector<T> implements List<T> {
  private static final int INIT_CAP = 2;
  private static final int CAP_MULT = 2;

  private int size;
  private int cap;
  private Object[] data;

  public Vector() {
    this(INIT_CAP);
  }

  public Vector(int cap) {
    this.cap = cap;
    this.data = new Object[cap];
    this.size = 0;
  }

  private void checkIndex(int i) {
    if (i < 0 || i >= size)
      throw new IndexOutOfBoundsException(i);
  }

  public T get(int i) {
    checkIndex(i);
    return (T) data[i];
  }

  public T set(int i, T item) {
    checkIndex(i);
    T old = (T) data[i];
    data[i] = item;
    return old;
  }

  public boolean push(T v) {
    if (size == cap)
      increase();
    data[size++] = v;
    return true;
  }

  public T pop() {
    if (isEmpty())
      return null;
    T out = (T) data[--size];
    data[size] = null;
    return out;
  }

  public T peek() {
    if (isEmpty())
      return null;
    return (T) data[size - 1];
  }

  private void increase() {
    resize(Math.max(cap * CAP_MULT, cap + 1));
  }

  public int size() {
    return size;
  }

  public int cap() {
    return cap;
  }

  public void resize(int cap) {
    data = Arrays.copyOf(data, cap);
    if (size > cap)
      size = cap;
    this.cap = cap;
  }

  public boolean contains(T item) {
    return indexOf(item) != -1;
  }

  public int indexOf(T item) {
    for (int i = 0; i < size; ++i)
      if (Objects.equals(data[i], item))
        return i;
    return -1;
  }

  public void add(int i, T item) {
    if (i < 0 || i > size)
      throw new IndexOutOfBoundsException(i);
    if (size == cap)
      increase();
    for (int j = size; j > i; --j)
      data[j] = data[j - 1];
    data[i] = item;
    ++size;
  }

  @Override
  public T remove(int i) {
    checkIndex(i);
    T out = (T) data[i];
    for (int j = i; j < size - 1; ++j)
      data[j] = data[j + 1];
    data[--size] = null;
    return out;
  }

  @Override
  public boolean remove(T item) {
    int idx = indexOf(item);
    if (idx == -1)
      return false;
    remove(idx);
    return true;
  }

  @Override
  public boolean add(T item) {
    return push(item);
  }

  @Override
  public boolean addAll(Collection<? extends T> list) {
    boolean result = false;
    for (T v : list)
      result |= add(v);
    return result;
  }

  public void clear() {
    Arrays.fill(data, 0, size, null);
    size = 0;
  }
}
