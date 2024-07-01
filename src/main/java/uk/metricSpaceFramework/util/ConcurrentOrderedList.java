package uk.metricSpaceFramework.util;

import uk.ac.standrews.cs.utilities.Pair;

import java.util.*;

/**
 * @author al
 *         This class allows the creation of a list, up to a finite length,
 *         ordered (small to large) according to a second parameter provided at
 *         each element addition. It is handy, for instance, for creating a list
 *         of the n closest matches to a reference object from within a
 *         collection; a new OrderedList of size n is created, each element
 *         tested is added along with its distance from the reference object,
 *         after which the best n matches (and comparators) can be retrieved.
 *
 *         Code has the same interface as the OrderedList class from Richard.
 *
 * @param <E>
 *            the type of the list element
 * @param <I>
 *            the type of the comparator, must extend Comparable
 */
public class ConcurrentOrderedList<E, I extends Comparable<I>> {

    private final TreeMap<I,E> map;
    private final int limit;
    private I threshold;

    /**
     *
     * Creates a new OrderedList object
     *
     * @param limit
     *            the maximum number of elements and comparators kept for later
     *            retrieval
     */
    public ConcurrentOrderedList(int limit) {
        this.map = new TreeMap<>();
        this.limit = limit;
        this.threshold = null;
    }

    // clones another Ordered list with the ability to change size.
    private ConcurrentOrderedList(ConcurrentOrderedList<E,I> copy_from, int limit) {
        int how_many_to_copy = Math.min(limit,copy_from.size());
        this.limit = how_many_to_copy;
        this.map = new TreeMap<>(copy_from.map);
        while( map.size() > limit ) {
            map.remove(map.lastKey()); // remove the biggest
        }
        if (this.size() == this.limit) {
            this.threshold = map.lastKey();
        } else {
            this.threshold = null;
        }
    }

    // clones another Ordered list with the ability to change size.
    private ConcurrentOrderedList(ConcurrentOrderedList<E,I> copy_from, int limit, int how_many_to_copy) {
        how_many_to_copy = Math.min( Math.min(limit,how_many_to_copy), copy_from.size() );
        this.limit = how_many_to_copy;
        this.map = new TreeMap<>(copy_from.map);
        while( map.size() > limit ) {
            map.remove(map.lastKey()); // remove the biggest
        }
        if (this.size() == this.limit) {
            this.threshold = map.lastKey();
        } else {
            this.threshold = null;
        }
    }

    public synchronized ConcurrentOrderedList<E,I> copy(int limit) {
        return new ConcurrentOrderedList<>(this,limit);
    }

    public synchronized ConcurrentOrderedList<E,I> copy(int limit, int number_of_elements) {
        return new ConcurrentOrderedList<>(this,limit,number_of_elements);
    }

    /**
     * @param element
     *            the element to be added
     * @param comparator
     *            the value which will be used to place the element in the
     *            ordered list
     */
    public synchronized void add(E element, I comparator) {
        map.put(comparator, element);
        if (map.size() > this.limit) {
            map.remove(map.lastKey()); // remove the biggest
        }
        if (map.size() == this.limit) {
            this.threshold = map.lastKey();
        }
    }

    public synchronized boolean contains(E element) {
        return map.containsValue(element);
    }

    public synchronized I getThreshold() {
        return threshold;
    }

    public synchronized I getLargest() {
        if( size() == 0 ) {
            return null;
        }
        return map.lastKey();
    }

    public synchronized boolean isEmpty() {
        return map.isEmpty();
    }

    public synchronized int size() {
        return map.size();
    }

    /**
     * @return the comparators of the elements added in ascending key order.
     * For backwards compatability.
     * The method getComparatorsSet() returns a set without an extra copy
     */
    public synchronized List<I> getComparators() {
        return new ArrayList<>(getComparatorsSet()); // in ascending key order.
    }

    /**
     * @return the n elements added with the smallest comparators in ascending key order.
     * For backwards compatability.
     * The method getValueCollection() returns a set without an extra copy
     */
    public synchronized List<E> getList() {
        return new ArrayList( getValueCollection() );
    }

    public synchronized Set<I> getComparatorsSet() {
        return map.keySet();
    }

    public synchronized Collection<E> getValueCollection() {
        return map.values();
    }

    @Override
    public synchronized String toString() {
        StringBuffer res = new StringBuffer("[");
        for (E e :map.values()) {
            res.append(e.toString() + ",");
        }
        res.setCharAt(res.length() - 1, ']');
        res.append(" [");
        for (I c : map.keySet()) {
            res.append(c.toString() + ",");
        }
        res.setCharAt(res.length() - 1, ']');

        return res.toString();
    }

    public synchronized Pair<E,I> remove(int index) {
        Pair<E,I> entry = get(index);  // this method takes care of indices etc.
        map.remove( entry.Y() );
        if (map.size() < this.limit) {
            this.threshold = null;
        } else {
            this.threshold = map.lastKey();
        }
        return entry;
    }

    public synchronized Pair<E,I> get(int index) {
        if( map.size() >= 1 && index < size() ) {
            Optional<Map.Entry<I, E>> opt_entry = map.entrySet().stream().skip(index).findFirst(); // was -1
            if (opt_entry.isPresent()) {
                Map.Entry<I, E> entry = opt_entry.get();
                return new Pair<>( entry.getValue(), entry.getKey() );
            }
        }
        throw new IndexOutOfBoundsException();
    }
}
