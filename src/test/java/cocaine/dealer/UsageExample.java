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

import java.util.concurrent.TimeUnit;

import cocaine.dealer.util.Helper;

public class UsageExample {
    public static void main(String[] args) {

        String appHandlePath = "myapp/myhandle";
        Dealer dealer = null;
        byte[] message = "helloworld".getBytes();
        MessagePolicy messagePolicy = MessagePolicy.builder()
                .maxRetries(10)
                .timeout(10, TimeUnit.SECONDS)
                .deadline(10, TimeUnit.DAYS)
                .build();
        try {
            dealer = Helper.createDealer();
            Response response = null;
            try {
                response = dealer.sendMessage(appHandlePath, message,
                        messagePolicy);
                for (byte[] chunk : response.asIterable(10, TimeUnit.SECONDS)) {
                    System.out.println(new String(chunk));
                }
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } finally {
            if (dealer != null)
                dealer.close();
        }
    }
}
