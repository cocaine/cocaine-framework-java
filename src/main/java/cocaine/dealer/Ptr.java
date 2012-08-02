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

/**
 * @author Vladimir Shakhov <bogdad@gmail.com>
 */
class Ptr {
    private long ptr=0;
    private boolean isInit = false;

    public Ptr(long ptr) {
        this.ptr = ptr;
        this.isInit = true;
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
