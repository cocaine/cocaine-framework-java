package cocaine.msgpack;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Map;

import cocaine.ServiceApi;
import cocaine.ServiceInfo;
import com.google.common.collect.ImmutableBiMap;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.unpacker.Unpacker;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ServiceInfoTemplate extends AbstractTemplate<ServiceInfo> {

    private final String name;

    private ServiceInfoTemplate(String name) {
        this.name = name;
    }

    public static Template<ServiceInfo> create(String name) {
        return new ServiceInfoTemplate(name);
    }

    @Override
    public void write(Packer packer, ServiceInfo service, boolean required) throws IOException {
        throw new UnsupportedOperationException(ServiceInfo.class.getSimpleName()
                + " can not be encoded by " + ServiceInfoTemplate.class.getSimpleName());
    }

    @Override
    public ServiceInfo read(Unpacker unpacker, ServiceInfo service, boolean required) throws IOException {
        unpacker.readArrayBegin();
        SocketAddress endpoint = unpacker.read(SocketAddressTemplate.getInstance());
        unpacker.readInt();
        Map<Integer, String> api = unpacker.read(Templates.tMap(Templates.TInteger, Templates.TString));
        unpacker.readArrayEnd();

        return new ServiceInfo(name, endpoint, ServiceApi.of(name, ImmutableBiMap.copyOf(api).inverse()));
    }

}

