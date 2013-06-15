package cocaine.msgpack;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.unpacker.Unpacker;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class SocketAddressTemplate extends AbstractTemplate<SocketAddress> {

    private static final Template<SocketAddress> instance = new SocketAddressTemplate();

    private SocketAddressTemplate() { }

    public static Template<SocketAddress> getInstance() {
        return instance;
    }

    @Override
    public void write(Packer packer, SocketAddress endpoint, boolean required) throws IOException {
        if (!InetSocketAddress.class.isAssignableFrom(endpoint.getClass())) {
            throw new IllegalArgumentException("Can only pack InetSocketAddress");
        }

        InetSocketAddress inetEndpoint = (InetSocketAddress) endpoint;
        packer.writeArrayBegin(2);
        packer.write(inetEndpoint.getHostName());
        packer.write(inetEndpoint.getPort());
        packer.writeArrayEnd();
    }

    @Override
    public SocketAddress read(Unpacker unpacker, SocketAddress endpoint, boolean required) throws IOException {
        unpacker.readArrayBegin();
        String host = unpacker.readString();
        int port = unpacker.readInt();
        unpacker.readArrayEnd();

        return new InetSocketAddress(host, port);
    }

}
