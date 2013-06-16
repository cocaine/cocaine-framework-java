package cocaine;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class LocatorResolveException extends CocaineException {

    private final String serviceName;
    private final SocketAddress endpoint;
    private final String reason;

    public LocatorResolveException(String serviceName, String host, int port, String reason) {
        super(serviceName + " - " +  host + ":" + port + " - " + reason);
        this.serviceName = serviceName;
        this.endpoint = new InetSocketAddress(host, port);
        this.reason = reason;
    }

    public LocatorResolveException(String serviceName, SocketAddress endpoint, String reason) {
        super(serviceName + " - " +  endpoint + " - " + reason);
        this.serviceName = serviceName;
        this.endpoint = endpoint;
        this.reason = reason;
    }

    public LocatorResolveException(String serviceName, String host, int port, Throwable reason) {
        super(serviceName + " - " +  host + ":" + port + " - " + reason.getLocalizedMessage(), reason);
        this.serviceName = serviceName;
        this.endpoint = new InetSocketAddress(host, port);
        this.reason = reason.getLocalizedMessage();
    }

    public LocatorResolveException(String serviceName, SocketAddress endpoint, Throwable reason) {
        super(serviceName + " - " +  endpoint + " - " + reason.getLocalizedMessage(), reason);
        this.serviceName = serviceName;
        this.endpoint = endpoint;
        this.reason = reason.getLocalizedMessage();
    }

    public String getServiceName() {
        return serviceName;
    }

    public SocketAddress getEndpoint() {
        return endpoint;
    }

    public String getReason() {
        return reason;
    }
}
