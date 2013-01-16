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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *  Response object allows for retrieving output of cocaine-apps after sending a message
 * see src/test/java/cocaine/dealer/UsageExample.java for usage example
 * 
 * @author Vladimir Shakhov <bogdad@gmail.com>
 */
public interface Response {

    /**
     * Returns a concatenated byte[] out of chunks returned by the cocaine app 
     */
    byte[] getAllChunks(long timeout, TimeUnit timeUnit)
            throws TimeoutException;

    /**
     * Returns a concatenated byte[] out of chunks returned by the cocaine app 
     */
    byte[] getAllChunks() throws TimeoutException;

    /**
     * Returns an iterable for traversing the byte[] chunks that might come from a cocaine app
     * Note: the iterator returned by the iterable might throw Timeout exception upon iterating 
     * the iterator is not thread safe! 
     */
    Iterable<byte[]> asIterable(long timeout, TimeUnit timeUnit);

    Iterable<byte[]> asIterable();

    /**
     * should call close() after use
     */
    void close();

}