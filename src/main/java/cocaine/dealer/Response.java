/*
    Copyright (c) 2012 Vladimir Shakhov <bogdad@gmail.com>
    Copyright (c) 2012 Other contributors as noted in the AUTHORS file.

    This file is part of Cocaine.

    Cocaine is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    Cocaine is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>. 
*/
package cocaine.dealer;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Response object allows for retrieving output of cocaine-apps after sending a message
 * see src/test/java/cocaine/dealer/UsageExample.java for usage example
 * 
 * @author Vladimir Shakhov <bogdad@gmail.com>
 */
public class Response {
    final Ptr cResponsePtr;
    private final Lock lock = new ReentrantLock();

    Response(long cResponsePtr) {
        this.cResponsePtr = new Ptr(cResponsePtr);
    }

    /**
     * Returns a concatenated byte[] out of chunks returned by a cocaine app 
     */
    public byte[] getAllChunks(long timeout, TimeUnit timeUnit) throws TimeoutException {
        lock.lock();
        long milliseconds = timeUnit.toMillis(timeout);
        // cocaineTimeout is in seconds
        double cocaineTimeout = milliseconds / 1000.0;
        try {
            if (!cResponsePtr.isReferring()) {
                throw new IllegalStateException("Response is closed");
            }
            return nativeGetAllChunks(cResponsePtr.get(), cocaineTimeout * 2);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Returns a concatenated byte[] out of chunks returned by a cocaine app 
     */
    public byte[] getAllChunks() throws TimeoutException {
        return getAllChunks(-1, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns an iterable for traversing the byte[] chunks that might come from a cocaine app
     * Note: the iterator returned by the iterable might throw Timeout exception upon iterating 
     * the iterator is not thread safe! 
     */
    public Iterable<byte[]> asIterable(long timeout, TimeUnit timeUnit) {
        return new ResponseIterable(this, timeout, timeUnit);
    }

    private boolean get(ArrayHolder data, long timeout, TimeUnit timeUnit) throws TimeoutException {
        lock.lock();
        long milliseconds = timeUnit.toMillis(timeout);
        // cocaineTimeout should be in seconds
        double cocaineTimeout = milliseconds / 1000.0;
        try {
            if (!cResponsePtr.isReferring()) {
                throw new IllegalStateException("Response is closed");
            }
            data.array = null;
            return nativeGet(data, cResponsePtr.get(), cocaineTimeout * 2);
        } finally {
            lock.unlock();
        }
    }

    /**
     * should call close() after use
     */
    public void close() {
        lock.lock();
        try {
            if (cResponsePtr.isReferring()) {
                nativeClose(cResponsePtr.get());
                cResponsePtr.close();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    void removedStoredMessageFor(long cDealerPtr) {
        lock.lock();
        try{
            nativeRemoveStoredMessageFor(cDealerPtr, cResponsePtr.get());
        } finally {
            lock.unlock();
        }
    }

    private native byte[] nativeGetAllChunks(long cResponsePtr, double timeout)
            throws TimeoutException;

    private native boolean nativeGet(ArrayHolder data, long cResponsePtr, double timeout)
            throws TimeoutException;

    private native int nativeRemoveStoredMessageFor(long cDealerPtr, long cResponsePtr);

    private native void nativeClose(long cResponsePtr);

    class ResponseIterable implements Iterable<byte[]> {

        private final Response response;
        private final long timeout;
        private final TimeUnit timeUnit;

        public ResponseIterable(Response response, long timeout, TimeUnit timeUnit) {
            this.response = response;
            this.timeout = timeout;
            this.timeUnit = timeUnit;
        }

        @Override
        public Iterator<byte[]> iterator() {
            return new ResponseChunkIterator(response, timeout, timeUnit);
        }

    }
    /**
     * Iterator of byte[] chunks that a cocaine app might return
     */
    class ResponseChunkIterator implements Iterator<byte[]>{
        private final Response response;
        private final long timeout;
        private TimeUnit timeUnit;
        private NextChunk nextChunk = null;

        
        public ResponseChunkIterator(Response response, long timeout, TimeUnit timeUnit) {
            this.response = response;
            this.timeout = timeout;
            this.timeUnit = timeUnit;
        }

        private void ensureNext() {
            if (nextChunk!=null)
                return;
            nextChunk = new NextChunk();
            try{
                nextChunk.hasNext= response.get(nextChunk, timeout, timeUnit);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
        
        @Override
        public boolean hasNext() {
            ensureNext();
            return nextChunk.hasNext;
        }

        @Override
        public byte[] next() {
            ensureNext();
            if (!hasNext())
                throw new NoSuchElementException();
            byte[] data = nextChunk.array;
            nextChunk = null;
            return data;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        class NextChunk extends ArrayHolder{
            public boolean hasNext = false;
        }
    }
}
