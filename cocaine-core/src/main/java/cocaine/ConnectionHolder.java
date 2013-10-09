package cocaine;

import java.net.SocketAddress;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import cocaine.netty.ServiceMessageHandler;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import org.apache.log4j.Logger;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ConnectionHolder {

    private static final Logger logger = Logger.getLogger(ConnectionHolder.class);

    private final ReadWriteLock lock;
    private final Bootstrap bootstrap;
    private final ServiceMessageHandler handler;
    private final Supplier<ServiceInfo> info;

    private ChannelFuture connection;
    private ServiceInfo serviceInfo;

    private ConnectionHolder(String service, Bootstrap bootstrap, Sessions sessions, Supplier<ServiceInfo> info) {
        this.info = info;
        this.lock = new ReentrantReadWriteLock(true);
        this.bootstrap = bootstrap;
        this.handler = new ServiceMessageHandler(service, this, sessions);
    }

    public void write(Object message) {
        Preconditions.checkState(connection != null, "Service is not connected");
        logger.debug("Writing " + message);

        lock.readLock().lock();
        try {
            connection.channel().write(message);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void connect() {
        reconnect();
    }

    public void reconnect() {
        serviceInfo = info.get();
        reconnect(serviceInfo.getEndpoint());
    }

    public ServiceInfo getServiceInfo() {
        Preconditions.checkState(serviceInfo != null, "Service is not connected");
        return serviceInfo;
    }

    private void reconnect(SocketAddress endpoint) {
        logger.debug("Reconnecting to " + endpoint);

        lock.writeLock().lock();
        try {
            connection = bootstrap.connect(endpoint).sync();
            connection.channel().pipeline().addLast(handler);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CocaineException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static ConnectionHolder create(String service, Bootstrap bootstrap,
            Sessions sessions, Supplier<ServiceInfo> infoSupplier)
    {
        return new ConnectionHolder(service, bootstrap, sessions, infoSupplier);
    }
}
