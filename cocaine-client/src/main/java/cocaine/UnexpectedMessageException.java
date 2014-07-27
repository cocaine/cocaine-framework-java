package cocaine;

import cocaine.message.Message;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class UnexpectedMessageException extends ServiceException {

    private final Message msg;

    public UnexpectedMessageException(String service, Message msg) {
        super(service, "Unexpected message: " + msg.toString());
        this.msg = msg;
    }

    public Message getMsg() {
        return msg;
    }
}
