package com.github.betacoders.collections;

import java.util.Iterator;

public class HashSet<E> implements Set<E> {
  private static final Object DUMMY = new Object();
  private final Map<E, Object> map = new HashMap<>();

  @Override
  public boolean add(E e) {
    return map.put(e, DUMMY) == null;
  }

  @Override
  public boolean contains(E e) {
    return map.get(e) != null;
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public Iterator<E> iterator() {
    return map.keySet().iterator();
  }
}
