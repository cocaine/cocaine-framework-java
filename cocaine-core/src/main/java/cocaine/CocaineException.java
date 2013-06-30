package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class CocaineException extends RuntimeException {

    public CocaineException() { }

    public CocaineException(String message) {
        super(message);
    }

    public CocaineException(String message, Throwable cause) {
        super(message, cause);
    }

    public CocaineException(Throwable cause) {
        super(cause);
    }

}
