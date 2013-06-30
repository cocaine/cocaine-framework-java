package cocaine.message;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class ChokeMessage extends Message {

    public ChokeMessage(long session) {
        super(MessageType.CHOKE, session);
    }

    @Override
    public String toString() {
        return "ChokeMessage/" + getSession();
    }

}
