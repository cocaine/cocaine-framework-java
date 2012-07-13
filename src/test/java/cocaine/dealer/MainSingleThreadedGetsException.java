package cocaine.dealer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cocaine.dealer.exceptions.AppException;


public class MainSingleThreadedGetsException {
    private final static String CONFIG_PATH = "./src/test/resources/dealer_config.json";
    private final static String PATH = "python1/failing_handle";

    public static void main(String[] args) throws TimeoutException {
        TextMessage message = new TextMessage("hello world");
        MessagePolicy messagePolicy = MessagePolicy.builder()
                .timeout(100000, TimeUnit.MILLISECONDS).build();
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
                long begin = System.nanoTime();
                try {
                    r = dealer.sendMessage(appPath, message, messagePolicy);
                    String response = r.getString(100000, TimeUnit.MILLISECONDS);
                } catch (AppException e) {
                    long end = System.nanoTime();
                    cursum += (end - begin);
                    if (counter % 1000 == 0) {
                        String date = sdf.format(Calendar.getInstance().getTime());
                        System.out.println(date+" " + e.getMessage() + " " + total_counter + " "
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
