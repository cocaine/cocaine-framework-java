package cocaine.log4j;

import cocaine.Logging;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public class CocaineAppender extends AppenderSkeleton {

    private final Logging delegate;

    public CocaineAppender(Logging delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void append(LoggingEvent event) {
        String message = getLayout().format(event);
        delegate.append(event.getLevel(), event.getLoggerName(), message);
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

}
