package uk.metricSpaceFramework.util;


import uk.ac.standrews.cs.utilities.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author newrichard
 *
 *         This class allows the creation of a list, up to a finite length,
 *         ordered (small to large) according to a second parameter provided at
 *         each element addition. It is handy, for instance, for creating a list
 *         of the n closest matches to a reference object from within a
 *         collection; a new OrderedList of size n is created, each element
 *         tested is added along with its distance from the reference object,
 *         after which the best n matches (and comparators) can be retrieved.
 *
 * @param <E>
 *            the type of the list element
 * @param <I>
 *            the type of the comparator, must extend Comparable
 */
public class OrderedList<E, I extends Comparable<I>> {

    private final List<E> elements;
    private final List<I> comparators;
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
    public OrderedList(int limit) {
        this.elements = new ArrayList<>();
        this.comparators = new ArrayList<>();
        this.limit = limit;
        this.threshold = null;
    }

    // clones another Ordered list with the ability to change size.
    private OrderedList(OrderedList<E,I> copy_from, int limit) {
        int how_many_to_copy = Math.min(limit,copy_from.size());
        this.elements = new ArrayList<>( copy_from.elements.subList(0,how_many_to_copy) );
        this.comparators = new ArrayList<>( copy_from.comparators.subList(0,how_many_to_copy) );
        this.limit = limit;
        if (this.comparators.size() == this.limit) {
            this.threshold = this.comparators.get(this.limit - 1);
        } else {
            this.threshold = null;
        }

    }

    // clones another Ordered list with the ability to change size.
    private OrderedList(OrderedList<E,I> copy_from, int limit, int how_many_to_copy) {
        how_many_to_copy = Math.min( Math.min(limit,how_many_to_copy), copy_from.size() );
        this.elements = new ArrayList<>( copy_from.elements.subList(0,how_many_to_copy) );
        this.comparators = new ArrayList<>( copy_from.comparators.subList(0,how_many_to_copy) );
        this.limit = limit;
        if (this.comparators.size() == this.limit) {
            this.threshold = this.comparators.get(this.limit - 1);
        } else {
            this.threshold = null;
        }
    }

    public void addAll( OrderedList<E, I> other ) {
        for( int i = 0; i < other.size(); i++ ) {
            Pair<E, I> pair = other.get(i);
            this.add( pair.X(), pair.Y() );
        }
    }

    public synchronized OrderedList<E,I> copy(int limit) {
        return new OrderedList<>(this,limit);
    }

    public synchronized OrderedList<E,I> copy(int limit,int number_of_elements) {
        return new OrderedList<>(this,limit,number_of_elements);
    }

    public void addAllDifferent( OrderedList<E, I> other ) {
        for( int i = 0; i < other.size(); i++ ) {
            Pair<E, I> pair = other.get(i);
            E value = pair.X();
            if( ! this.getList().contains( value ) ) {
                I dist = pair.Y();
                this.add(value, dist);
            }
        }
    }

    /**
     * @param element
     *            the element to be added
     * @param comparator
     *            the value which will be used to place the element in the
     *            ordered list
     */
    public synchronized void add(E element, I comparator) {
        if (this.threshold == null || comparator.compareTo(this.threshold) <= 0) {
            if (this.comparators.size() == 0) {
                this.comparators.add(comparator);
                this.elements.add(element);
            } else {
                int ptr = 0;
                /*
                 * advance ptr until the node it's pointing at is greater than
                 * or equal to the comparator, or past the end of the list
                 */
                while (ptr < this.comparators.size()
                        && this.comparators.get(ptr).compareTo(comparator) <= 0) {
                    ptr++;
                }

                if (ptr < this.limit) {
                    this.comparators.add(ptr, comparator);
                    this.elements.add(ptr, element);
                }
                if (this.comparators.size() == this.limit) {
                    this.threshold = this.comparators.get(this.limit - 1);
                }
                if (this.comparators.size() > this.limit) {
                    this.threshold = this.comparators.get(this.limit - 1);
                    this.comparators.remove(this.limit);
                    this.elements.remove(this.limit);
                }
            }
        }
    }

    /**
     * @return the threshold required for a new item to be inserted; this might
     *         be more efficient than a call to add if boxing is required
     *         boxing. The result is null until the list is up to size, then the
     *         real threshold for insertion is returned so this can be checked
     *         before a call to "add" is required
     */
    public synchronized I getThreshold() {
        return this.threshold;
    }

    public synchronized I getLargest() {
        if( size() == 0 ) {
            return null;
        }
        int last_index = size() - 1;
        return this.comparators.get(last_index);
    }

    /**
     * @return the n elements added with the smallest comparators
     */
    public synchronized List<E> getList() {
        return this.elements;
    }

    public synchronized boolean isEmpty() {
        return this.elements.isEmpty();
    }

    public synchronized int size() {
        return this.elements.size();
    }

    /**
     * @return the comparators of the elements added
     */
    public List<I> getComparators() {
        return this.comparators;
    }

    @Override
    public synchronized String toString() {
        StringBuffer res = new StringBuffer("[");
        for (E e : this.elements) {
            res.append(e.toString() + ",");
        }
        res.setCharAt(res.length() - 1, ']');
        res.append(" [");
        for (I c : this.comparators) {
            res.append(c.toString() + ",");
        }
        res.setCharAt(res.length() - 1, ']');

        return res.toString();
    }

    public Pair<E,I> remove(int index) {
        if( size() >= 1 ) {
            int last_index = size() - 1;
            if (last_index == 0) {        // Going to delete the only entry
                this.threshold = null;
            } else if (index < 0 || index >= this.limit ) {  // strictly an error
                throw new IndexOutOfBoundsException();
            } else if (index == last_index) { // last entry is getting removed
                this.threshold = this.comparators.get(size() - 1);
            }
            // remove the element
            E data = this.elements.remove(index);
            I comparitor = this.comparators.remove(index);
            return new Pair<E,I>(data,comparitor);
        }
        throw new IndexOutOfBoundsException();
    }

    public Pair<E,I> get(int index) {
        if( size() >= 1 && index < size() ) {
            E data = this.elements.get(index);
            I comparitor = this.comparators.get(index);
            return new Pair<E,I>(data,comparitor);
        }
        throw new IndexOutOfBoundsException();
    }

}

