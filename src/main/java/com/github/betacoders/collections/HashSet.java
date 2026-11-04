package com.github.betacoders.collections;

public class HashSet<E> implements Set<E> {
  private final Map<E, Object> map = new HashMap<>();
  private int count;

  @Override
  public boolean add(E e) {
    if (map.get(e) != null) {
      return false;
    }
    map.put(e, Boolean.TRUE);
    ++count;
    return true;
  }

  @Override
  public boolean contains(E e) {
    return map.get(e) != null;
  }

  @Override
  public int size() {
    return count;
  }
}
