package bitdata.code.util;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.*;
import java.util.stream.Collectors;

/**
 * used to check a method is a source in call graph
 *
 * @param <E> method type
 */
public class BooleanGraph<E> {

    private Map<E, Boolean> flagMap = new HashMap<>();

    private MultiValuedMap<E, E> childrenMap = new ArrayListValuedHashMap<>();

    private Set<E> set = new HashSet<>();

    public boolean contains(E element) {
        return flagMap.containsKey(element);
    }

    public Collection<E> getNodes() {
        return flagMap.keySet();
    }

    public MultiValuedMap<E, E> getEdges(){
        return childrenMap;
    }

    public Collection<E> filter(boolean flag) {
        return flagMap.entrySet().stream().filter(e -> e.getValue() == flag).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public void setFlag(E element, boolean flag) {
        flagMap.put(element, flag);
    }

    public void addChild(E parent, E child) {
        childrenMap.put(parent, child);
    }

    public void setTreeFlag(boolean flag) {
        set.clear();
        for (Map.Entry<E, Boolean> entry : flagMap.entrySet()) {
            E element = entry.getKey();
            if (!set.contains(element)) {
                Boolean v = entry.getValue();
                if (v.equals(flag)) {
                    setChildrenFlag(element, flag);
                }
            }
        }
    }

    private void setChildrenFlag(E element, boolean flag) {
        Collection<E> children = childrenMap.get(element);
        if (children != null) {
            for (E e : children) {
                Boolean v = flagMap.get(e);
                if (!v.equals(flag)) {
                    flagMap.put(e, flag);
                    setChildrenFlag(e, flag);
                    set.add(e);
                }
            }
        }
    }

}
