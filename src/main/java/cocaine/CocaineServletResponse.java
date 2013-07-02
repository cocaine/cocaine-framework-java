package cocaine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;

import cocaine.message.Messages;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class CocaineServletResponse implements ServletResponse {

    private final long session;
    private final ChannelHandlerContext ctx;
    private final ByteArrayOutputStream buffer;
    private final ServletOutputStream outputStream;

    private boolean isCommitted = false;

    public CocaineServletResponse(long session, ChannelHandlerContext ctx) {
        this.session = session;
        this.ctx = ctx;
        this.buffer = new ByteArrayOutputStream();
        this.outputStream = new CocaineOutputStream();
    }

    @Override
    public String getCharacterEncoding() {
        return "binary";
    }

    @Override
    public String getContentType() {
        return "application/x-msgpack";
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public void setCharacterEncoding(String charset) { }

    @Override
    public void setContentLength(int len) { }

    @Override
    public void setContentLengthLong(long len) { }

    @Override
    public void setContentType(String type) { }

    @Override
    public void setBufferSize(int size) { }

    @Override
    public int getBufferSize() {
        return buffer.size();
    }

    @Override
    public void flushBuffer() throws IOException {
        outputStream.flush();
    }

    @Override
    public void resetBuffer() {
        if (isCommitted()) {
            throw new IllegalStateException("Buffer has already been committed");
        }
        buffer.reset();
    }

    @Override
    public boolean isCommitted() {
        return isCommitted;
    }

    @Override
    public void reset() {
        resetBuffer();
    }

    @Override
    public void setLocale(Locale loc) { }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    private class CocaineOutputStream extends ServletOutputStream {

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) { }

        @Override
        public void write(int b) throws IOException {
            buffer.write(b);
        }

        @Override
        public void flush() throws IOException {
            synchronized (buffer) {
                ctx.write(Messages.chunk(session, buffer.toByteArray()));
                buffer.reset();
            }
            isCommitted = true;
        }

        @Override
        public void close() throws IOException {
            ctx.write(Messages.choke(session));
            isCommitted = true;
        }

    }

}
