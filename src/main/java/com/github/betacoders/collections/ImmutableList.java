package com.github.betacoders.collections;

import java.util.Objects;

public final class ImmutableList<T> implements List<T> {
    private final Vector<T> items;

    public ImmutableList(Collection<? extends T> source) {
        Objects.requireNonNull(source, "Source collection is required.");
        items = new Vector<>(source.size());

        for (T item : source) {
            items.add(
                Objects.requireNonNull(item, "Null items are not allowed.")
            );
        }
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public T get(int index) {
        return items.get(index);
    }

    public static boolean sameElements(List<?> first, List<?> second) {
        if (first.size() != second.size()) {
            return false;
        }

        for (int i = 0; i < first.size(); i++) {
            if (!Objects.equals(first.get(i), second.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
            || other instanceof ImmutableList<?> list
            && sameElements(this, list);
    }

    @Override
    public int hashCode() {
        int result = 1;

        for (T item : items) {
            result = 31 * result + item.hashCode();
        }

        return result;
    }
}