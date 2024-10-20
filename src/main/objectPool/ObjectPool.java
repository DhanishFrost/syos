package main.objectPool;

public interface ObjectPool<T> {
    T borrowObject();
    void returnObject(T obj);
}

