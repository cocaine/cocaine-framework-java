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

import cocaine.dealer.util.Helper;

public class PersistenceExample {
    private static final String SERVICE_ALIAS = "python1";
    private static final String APP_PATH = "python1/sleeping_handle";

    /**
     * prereqs: `cocained` not running what it does: send() sends a message,
     * then closes dealer, without waiting for responce
     * 
     * loadAndRemoveNotRespondedToMessages() constructs another dealer, and gets
     * persisted messages, displays them and then removes them
     * 
     * then checkNothingPersisted() asserts that no messages are persisted
     * 
     */
    public static void main(String[] args) throws InterruptedException {
        send();
        loadAndRemoveNotRespondedToMessages();
        checkNothingPersisted();
    }

    private static void send() {
        Dealer dealer = Helper.createDealer();
        MessagePolicy messagePolicy = MessagePolicy.builder().persistent()
                .maxRetries(10).build();
        Response response = dealer.sendMessage(APP_PATH, "hello!".getBytes(),
                messagePolicy);
        response.close();
        dealer.close();
    }

    private static void loadAndRemoveNotRespondedToMessages() {
        Dealer dealer = Helper.createDealer();
        try {
            int messageCount = dealer.getStoredMessagesCount(SERVICE_ALIAS);
            System.out.println("saved messages " + messageCount);
            if (messageCount == 0) {
                throw new RuntimeException("should have persisted the message");
            }
            List<Message> messages = dealer.getStoredMessages(SERVICE_ALIAS);
            for (Message message : messages) {
                MessagePolicy policy = message.getPolicy();
                System.out.println("====");
                System.out.println(message.getId());
                System.out.println(policy.toString());
                System.out.println(message.getService());
                System.out.println(message.getHandle());
                System.out.println(new String(message.getData()));
                dealer.removeStoredMessage(message);
            }
        } finally {
            dealer.close();
        }
    }

    private static void checkNothingPersisted() {
        Dealer dealer = Helper.createDealer();
        try {
            int messageCount = dealer.getStoredMessagesCount(SERVICE_ALIAS);
            if (messageCount > 0) {
                throw new RuntimeException("persistent storage should be empty");
            }
        } finally {
            dealer.close();
        }
    }

}
