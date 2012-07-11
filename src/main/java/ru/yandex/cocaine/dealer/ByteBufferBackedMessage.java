package ru.yandex.cocaine.dealer;

import java.nio.ByteBuffer;

public class ByteBufferBackedMessage implements Message{

    private final ByteBuffer buffer;
    
    public ByteBufferBackedMessage(ByteBuffer buffer) {
        this.buffer = buffer;
    }
    
    public ByteBuffer getByteBuffer() {
        return buffer;
    }
    
    @Override
    public byte[] getBytes() {
        throw new RuntimeException("should not be called");
    }

}
