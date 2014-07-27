package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ServiceErrorException extends ServiceException {

    private final int code;

    public ServiceErrorException(String service, String message, int code) {
        super(service, code + " - " + message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
