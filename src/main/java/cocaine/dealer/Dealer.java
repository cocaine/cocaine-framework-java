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

import java.util.List;

/** 
 * see src/test/java/cocaine/dealer/UsageExample.java for usage example
 *
 * @author Vladimir Shakhov <bogdad@gmail.com>
 */
public interface Dealer {

    List<Response> sendMessages(String path, byte[] message,
            MessagePolicy policy);

    List<Response> sendMessages(String path, byte[] message);

    /**
     * send a message(byte[]) to the specified cocaine app/path [='app/handle']
     * with a specified MessagePolicy
     * and returns a Response object for retrieving results
     */
    Response sendMessage(String path, byte[] message,
            MessagePolicy messagePolicy);

    /**
     * send a message(byte[]) to the specified cocaine app/path [='app/handle']
     * and returns a Response object for retrieving results
     */
    Response sendMessage(String path, byte[] message);

    /**
     * get the stored messages for this app.
     * DealerImpl stores messages in persistent storage
     * until it receives a response from cocaine node.
     * 
     * Upon starting dealer up it is convinient to load
     * all the persisted messages, and either resend them,
     * or just remove from the store. 
     * 
     * this feature is turned off by default.
     * to turn it on (on per message basis) :
     *  `"use_persistense": true` in dealer_config.json
     * and set persistent: true in the MessagePolicy for the message sent 
     * 
     */
    List<Message> getStoredMessages(String app);

    /**
     * removes stored message for this Response
     */
    void removeStoredMessageFor(Response response);

    /**
     * removes stored message for this Message
     */
    void removeStoredMessage(Message message);

    /**
     * get the number of stored messages for this app
     * 
     */
    int getStoredMessagesCount(String app);

    /**
     * retrieve policy defined for service in dealer_config.json 
     *
     */
    MessagePolicy policyForService(String serviceAlias);

    /**
     * returns the config path, the dealer was created with
     */
    String getConfigPath();

    /**
     * user should call close()
     */
    void close();

}