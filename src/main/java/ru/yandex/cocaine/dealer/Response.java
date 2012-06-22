package ru.yandex.cocaine.dealer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Vladimir Shakhov <vshakhov@yandex-team.ru>
 */
public class Response {
    private Ptr cResponsePtr;
    private long timeout;
    private TimeUnit timeUnit;

    Response(long cResponsePtr) {
        this.cResponsePtr = new Ptr(cResponsePtr);
    }

    public String get(long timeout, TimeUnit timeUnit) throws TimeoutException {
        if (!cResponsePtr.isReferring()) {
            throw new IllegalStateException("Response is closed");
        }

        long milliseconds = timeUnit.toMillis(timeout);
        // see response_impl.cpp: response_impl_t::get for cocaineTimeout
        // cocaineTimeout==1 equals to 1000 seconds
        double cocaineTimeout = milliseconds / 1000000.0;
        return get(cResponsePtr.get(), cocaineTimeout * 2);
    }

    public void close() {
        if (cResponsePtr.isReferring()) {
            close(cResponsePtr.get());
        }
        cResponsePtr.close();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    private native String get(long cResponsePtr, double timeout)
            throws TimeoutException;

    private native void close(long cResponsePtr);

    {
        System.loadLibrary("cocaine-framework-java");
    }
}
