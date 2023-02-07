package com.jkddg.nvrmailclient.model;


import java.util.concurrent.LinkedBlockingQueue;


/**
 * @Author 黄永好
 * @create 2023/2/7 12:26
 */
public class FixedQueue<E> extends LinkedBlockingQueue<E> {
    private final int capacity;
    public FixedQueue(int capacity) {
        super(capacity);
        this.capacity= capacity;
    }



    @Override
    public boolean add(E element) {
        if (this.size() == capacity) {
            poll();
        }
        return super.add(element);
    }

}
