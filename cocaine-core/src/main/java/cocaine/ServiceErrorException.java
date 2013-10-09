package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ServiceErrorException extends ServiceException {

    private final int code;

    public ServiceErrorException(String serviceName, String message, int code) {
        super(serviceName, code + " - " + message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
