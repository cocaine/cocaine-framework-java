package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ClientErrorException extends ClientException {

    private final int code;

    public ClientErrorException(String application, String message, int code) {
        super(application, code + " - " + message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
