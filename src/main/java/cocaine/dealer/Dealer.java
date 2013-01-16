package cocaine.dealer;

import java.util.List;

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
     * user should call close()
     */
    void close();

}