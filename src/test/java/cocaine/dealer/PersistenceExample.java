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
