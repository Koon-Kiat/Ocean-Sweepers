package project.game.context.api.pool;

import java.util.Queue;
import java.util.LinkedList;

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