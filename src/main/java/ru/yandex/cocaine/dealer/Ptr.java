package ru.yandex.cocaine.dealer;

public class Ptr {
    private long ptr=0;
    private boolean isInit = false;

    public Ptr(long ptr) {
        this.ptr = ptr;
        this.isInit = true;
    }

    public Ptr(){
        this.isInit = false;
    }
    
    public void close() {
        isInit = false;
    }
    
    public boolean isReferring(){
        return this.isInit;
    }
    
    public long get() {
        if (!isInit)
            throw new IllegalStateException("already closed");
        return ptr;
    }
}
