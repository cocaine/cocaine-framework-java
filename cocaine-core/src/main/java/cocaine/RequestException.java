package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class RequestException extends CocaineException {

    public RequestException(String message) {
        super(message);
    }

    public RequestException(Throwable throwable) {
        super(throwable);
    }

}
