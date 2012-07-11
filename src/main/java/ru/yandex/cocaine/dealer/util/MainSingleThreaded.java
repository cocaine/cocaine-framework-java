package ru.yandex.cocaine.dealer.util;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ru.yandex.cocaine.dealer.ByteBufferBackedMessage;
import ru.yandex.cocaine.dealer.Dealer;
import ru.yandex.cocaine.dealer.MessagePolicy;
import ru.yandex.cocaine.dealer.Response;
import ru.yandex.cocaine.dealer.TextMessage;

public class MainSingleThreaded {
    private final static String CONFIG_PATH = "./src/test/resources/dealer_config.json";
    private final static String PATH = "app1/test_handle";

    public static void main(String[] args) throws TimeoutException {
        TextMessage message = new TextMessage("hello world");
        MessagePolicy messagePolicy = MessagePolicy.builder()
                .timeout(100000, TimeUnit.MILLISECONDS).build();
        ByteBuffer buffer = ByteBuffer.allocateDirect(1000);
        buffer.asCharBuffer().append("hi");
        ByteBufferBackedMessage bbMsg = new ByteBufferBackedMessage(buffer);
        Dealer dealer = null;
        long cursum = 0;
        String appPath = PATH;
        if (args.length>0) {
            appPath = args[0];
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            dealer = new Dealer(CONFIG_PATH);
            int counter = 1;
            int total_counter = 1;
            for (;;) {
                Response r = null;
                try {
                    long begin = System.nanoTime();
                    r = dealer.sendMessage(appPath, bbMsg, messagePolicy);
                    String response = r.get(100000, TimeUnit.MILLISECONDS);
                    long end = System.nanoTime();
                    cursum += (end - begin);
                    if (counter % 1000 == 0) {
                        String date = sdf.format(Calendar.getInstance().getTime());
                        System.out.println(date+" " + response + " " + total_counter + " "
                                + ((cursum) / (counter * 1000000.0)));
                    }
                    if (counter % 10000 == 0) {
                        cursum = cursum / counter;
                        counter = 1;
                    }
                } finally {
                    if (r != null) {
                        r.close();
                    }
                }
                counter++;
                total_counter++;
            }
        } finally {
            if (dealer != null) {
                dealer.close();
            }
        }
    }
}
