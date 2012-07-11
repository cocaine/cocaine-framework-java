package ru.yandex.cocaine.dealer;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Ptr {
    private final Lock globalLock = new ReentrantLock();
    private long ptr=0;
    private boolean isInit = false;

    public Ptr(long ptr) {
        globalLock.lock();
        try {
            this.ptr = ptr;
            this.isInit = true;
        } finally {
            globalLock.unlock();
        }
    }

    public void close() {
        globalLock.lock();
        try{
            isInit = false;
        } finally {
            globalLock.unlock();
        }
    }

    public boolean isReferring(){
        globalLock.lock();
        try{
            return this.isInit;
        } finally {
            globalLock.unlock();
        }
    }

    public long get() {
        globalLock.lock();
        try{
            if (!isInit)
                throw new IllegalStateException("already closed");
            return ptr;
        } finally {
            globalLock.unlock();
        }
    }
}
