package cocaine;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public class ClientException extends CocaineException {

    private final String application;

    public ClientException(String application, String message) {
        super(application + " - " + message);
        this.application = application;
    }

    public String getApplication() {
        return application;
    }

}
