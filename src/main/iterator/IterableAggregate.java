package main.iterator;

public interface IterableAggregate<T> {
    Iterator<T> createIterator();
}
