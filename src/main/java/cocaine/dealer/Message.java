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

public class Message {

    private final String id;
    private final String service;
    private final String handle;
    private final byte[] data;
    private final MessagePolicy policy;

    public Message(String id, String service, String handle, byte[] data, MessagePolicy policy) {
        this.id = id;
        this.service = service;
        this.handle = handle;
        this.data = data;
        this.policy = policy;
    }

    public String getId() {
        return id;
    }

    public String getService() {
        return service;
    }

    public String getHandle() {
        return handle;
    }

    public byte[] getData() {
        return data;
    }

    public MessagePolicy getPolicy() {
        return policy;
    }

}
