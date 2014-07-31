package cocaine;

import cocaine.message.Message;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class UnexpectedClientMessageException extends ClientException {

    private final Message msg;

    public UnexpectedClientMessageException(String application, Message msg) {
        super(application, "Unexpected message: " + msg.toString());
        this.msg = msg;
    }

    public Message getMsg() {
        return msg;
    }
}
