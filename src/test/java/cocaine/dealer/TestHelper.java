/*
    Copyright (c) 2012 Vladimir Shakhov <bogdad@gmail.com>
    Copyright (c) 2012 Other contributors as noted in the AUTHORS file.

    This file is part of Cocaine.

    Cocaine is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    Cocaine is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>. 
*/
package cocaine.dealer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import cocaine.dealer.exceptions.AppException;

public class TestHelper {

    public static void timeIt(Callable<String> callable) throws Exception {
        int blockSize = 1000;
        int printAfter = 10000;
        long i = 1;
        long curSum = 0;
        int iInBlock = 0;
        int iBlock = 0;
        double blockSum = 0;
        SimpleDateFormat sdf = new SimpleDateFormat();
        for (;;) {
            long beg = System.nanoTime();
            String response = callable.call();
            long end = System.nanoTime();
            curSum += end - beg;
            if (i % blockSize == 0) {
                String date = sdf.format(Calendar.getInstance().getTime());
                double blockAvg = (double) curSum / iInBlock;
                iInBlock = 0;
                curSum = 0;
                blockSum += blockAvg;
                iBlock++;
                System.out.println(date + " i:" + i + " blockAvg: " + blockAvg
                        / 1000000 + " response: " + response);
            }
            if (i % printAfter == 0) {
                String date = sdf.format(Calendar.getInstance().getTime());
                double avg = (double) blockSum / iBlock;
                System.out.println(date + " i: " + i + " avg " + avg / 1000000
                        + " " + response);
            }
            i++;
            iInBlock++;
        }
    }

    public static byte[] getLargeMessage() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            builder.append("helloworld" + i);
        }
        byte[] message = builder.toString().getBytes();
        return message;
    }

    public static Callable<String> responseGetStringCallable(
            final Dealer dealer, final String path, final byte[] message,
            final MessagePolicy messagePolicy) {
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                Response r = null;
                try {
                    r = dealer.sendMessage(path, message, messagePolicy);
                    return r.getString(10000, TimeUnit.SECONDS);
                } finally {
                    if (r != null) {
                        r.close();
                    }
                }
            }
        };
    }

    public static Callable<String> responseGetLoopCallable(final Dealer dealer,
            final String path, final byte[] message,
            final MessagePolicy messagePolicy) {
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                Response r = null;
                try {
                    r = dealer.sendMessage(path, message, messagePolicy);
                    String response = "";
                    for (Iterator<byte[]> iter = r.getIterator(10000, TimeUnit.SECONDS); iter.hasNext(); ) {
                        response = response + new String(iter.next());
                    }
                    return response;
                } finally {
                    if (r != null) {
                        r.close();
                    }
                }
            }
        };
    }

    public static Callable<String> responseGetLoopExceptionCallable(final Dealer dealer, final String path, final byte[] message, final MessagePolicy messagePolicy) {
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                Response r = null;
                try{
                    r = dealer.sendMessage(path, message, messagePolicy);
                    String response = "";
                    for (Iterator<byte[]> iter = r.getIterator(10000, TimeUnit.SECONDS); iter.hasNext();) {
                        response = response + new String(iter.next());
                    }
                    return response;
                } catch (AppException e) {
                    return e.getMessage();
                } finally {
                    if (r!=null) {
                        r.close();
                    }
                }
            }
        };
    }
}
