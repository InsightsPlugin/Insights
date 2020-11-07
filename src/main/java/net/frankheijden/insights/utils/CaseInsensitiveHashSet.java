package net.frankheijden.insights.utils;

import java.util.Collection;
import java.util.HashSet;

public class CaseInsensitiveHashSet extends HashSet<String> {

    public CaseInsensitiveHashSet() {
        super();
    }

    public CaseInsensitiveHashSet(Collection<? extends String> c) {
        super(c.size());
        this.addAll(c);
    }

    public CaseInsensitiveHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CaseInsensitiveHashSet(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public boolean contains(Object o) {
        return super.contains(o.toString().toUpperCase());
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o.toString().toUpperCase())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean add(String e) {
        return super.add(e.toUpperCase());
    }

    @Override
    public boolean addAll(Collection<? extends String> c) {
        boolean added = false;
        for (String s : c) {
            added |= add(s);
        }
        return added;
    }

    @Override
    public boolean remove(Object o) {
        return super.remove(o.toString().toUpperCase());
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean removed = false;
        for (Object s : c) {
            removed |= remove(s.toString());
        }
        return removed;
    }
}
