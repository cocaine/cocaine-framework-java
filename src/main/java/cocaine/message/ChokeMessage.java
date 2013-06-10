package cocaine.message;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ChokeMessage extends Message {

    public ChokeMessage(long session) {
        super(Type.CHOKE, session);
    }

    @Override
    public String toString() {
        return "ChokeMessage/" + getSession();
    }

}
