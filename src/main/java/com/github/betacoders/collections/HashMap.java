package com.github.betacoders.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class HashMap<K, V> implements Map<K, V> {
  private static final int INIT_CAP = 16;

  private Object[] keys;
  private Object[] values;
  private int size;
  private int cap;

  private Set<K> keySet;
  private Collection<V> valuesCollection;
  private Set<Entry<K, V>> entrySet;

  public HashMap(int cap) {
    this.cap = cap;
    this.clear();
  }

  public HashMap() {
    this(INIT_CAP);
  }

  private int index(K key) {
    return (key.hashCode() & 0x7fffffff) % cap;
  }

  private void grow() {
    Object[] oldKeys = keys;
    Object[] oldValues = values;

    int oldCap = cap;
    this.cap = oldCap * 2;

    keys = new Object[cap];
    values = new Object[cap];
    size = 0;
    for (int i = 0; i < oldCap; ++i)
      if (oldKeys[i] != null)
        put((K) oldKeys[i], (V) oldValues[i]);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public V get(K key) {
    if (key == null)
      return null;

    int i = index(key);
    while (keys[i] != null) {
      if (key.equals(keys[i]))
        return (V) values[i];

      i = (i + 1) % cap;
    }
    return null;
  }

  @Override
  public V put(K key, V value) {
    if (key == null)
      return null;

    if ((size + 1) * 4 >= cap * 3)
      grow();

    int i = index(key);
    while (keys[i] != null) {
      if (key.equals(keys[i])) {
        V old = (V) values[i];
        values[i] = value;
        return old;
      }
      i = (i + 1) % cap;
    }
    keys[i] = key;
    values[i] = value;
    ++size;
    return null;
  }

  @Override
  public V remove(K key) {
    if (key == null)
      return null;

    int i = index(key);
    while (keys[i] != null) {
      if (key.equals(keys[i])) {
        V oldValue = (V) values[i];
        keys[i] = null;
        values[i] = null;
        --size;

        i = (i + 1) % cap;
        while (keys[i] != null) {
          K keyToRehash = (K) keys[i];
          V valToRehash = (V) values[i];
          keys[i] = null;
          values[i] = null;
          --size;
          put(keyToRehash, valToRehash);
          i = (i + 1) % cap;
        }

        return oldValue;
      }
      i = (i + 1) % cap;
    }

    return null;
  }

  @Override
  public void clear() {
    keys = new Object[cap];
    values = new Object[cap];
    size = 0;
  }

  @Override
  public Set<K> keySet() {
    if (keySet == null)
      keySet = new KeySet();
    return keySet;
  }

  @Override
  public Collection<V> values() {
    if (valuesCollection == null)
      valuesCollection = new Values();
    return valuesCollection;
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    if (entrySet == null)
      entrySet = new EntrySet();
    return entrySet;
  }

  private class KeySet implements Set<K> {
    @Override
    public int size() {
      return size;
    }

    @Override
    public Iterator<K> iterator() {
      return new Iterator<>() {
        private int i = 0;
        private int count = 0;

        @Override
        public boolean hasNext() {
          return count < size;
        }

        @Override
        public K next() {
          if (!hasNext())
            throw new NoSuchElementException();
          while (keys[i] == null)
            i++;
          count++;
          return (K) keys[i++];
        }
      };
    }
  }

  private class Values implements Collection<V> {
    @Override
    public int size() {
      return size;
    }

    @Override
    public Iterator<V> iterator() {
      return new Iterator<>() {
        private int i = 0;
        private int count = 0;

        @Override
        public boolean hasNext() {
          return count < size;
        }

        @Override
        public V next() {
          if (!hasNext())
            throw new NoSuchElementException();
          while (keys[i] == null)
            i++;
          count++;
          return (V) values[i++];
        }
      };
    }
  }

  private class EntrySet implements Set<Entry<K, V>> {
    @Override
    public int size() {
      return size;
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
      return new Iterator<>() {
        private int i = 0;
        private int count = 0;

        @Override
        public boolean hasNext() {
          return count < size;
        }

        @Override
        public Entry<K, V> next() {
          if (!hasNext())
            throw new NoSuchElementException();
          while (keys[i] == null)
            i++;
          count++;
          Entry<K, V> entry = new Entry<>((K) keys[i], (V) values[i]);
          i++;
          return entry;
        }
      };
    }
  }
}
