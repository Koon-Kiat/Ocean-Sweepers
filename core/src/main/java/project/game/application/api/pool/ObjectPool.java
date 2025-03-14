package project.game.application.api.pool;

import java.util.LinkedList;
import java.util.Queue;

public class ObjectPool<T> {
    private final Queue<T> pool;
    private final int maxSize;
    private final ObjectFactory<T> factory;

    public ObjectPool(int maxSize, ObjectFactory<T> factory) {
        this.pool = new LinkedList<>();
        this.maxSize = maxSize;
        this.factory = factory;
    }

    public T borrowObject() {
        if (pool.isEmpty()) {
            return factory.createObject();
        } else {
            return pool.poll();
        }
    }

    public void returnObject(T obj) {
        if (pool.size() < maxSize) {
            pool.offer(obj);
        }
    }

    public interface ObjectFactory<T> {
        T createObject();
    }
}