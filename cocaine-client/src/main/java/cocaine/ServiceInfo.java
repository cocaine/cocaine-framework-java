package cocaine;

import java.net.SocketAddress;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ServiceInfo {

    private final String name;
    private final SocketAddress endpoint;
    private final ServiceApi api;

    public ServiceInfo(String name, SocketAddress endpoint, ServiceApi api) {
        this.name = name;
        this.endpoint = endpoint;
        this.api = api;
    }

    public String getName() {
        return name;
    }

    public SocketAddress getEndpoint() {
        return endpoint;
    }

    public ServiceApi getApi() {
        return api;
    }

    @Override
    public String toString() {
        return "Service: " + name + "; Endpoint: " + endpoint + "; Service API: { " + api.toString() + " }";
    }
}
