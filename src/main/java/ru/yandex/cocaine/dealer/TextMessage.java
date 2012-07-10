package ru.yandex.cocaine.dealer;

/**
 * @author Vladimir Shakhov <vshakhov@yandex-team.ru>
 */
public class TextMessage implements Message {
    private final String message;

    public TextMessage(String message) {
        this.message = message;
    }

    @Override
    public byte[] getBytes() {
        return message.getBytes();
    }

}
