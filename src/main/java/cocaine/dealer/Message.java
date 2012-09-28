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
