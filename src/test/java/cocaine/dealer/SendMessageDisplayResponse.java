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
import java.util.concurrent.TimeoutException;

import cocaine.dealer.util.Helper;

public class SendMessageDisplayResponse {
    public static void main(String[] args) throws TimeoutException {
        if (args.length!=2) {
            System.out.println("Usage: SendMessageDisplayResponse <path> <messageData>");
            System.exit(0);
        }
        MessagePolicy policy = MessagePolicy.builder()
               .persistent()
               .maxRetries(10)
               .build();
        String path = args[0];
        String messageData = args[1];
        Dealer dealer = null;
        try {
            dealer = Helper.createDealer();
            System.out.println("dealer created successfully");
            Response response = null;
            try { 
                response = dealer.sendMessage(path, messageData.getBytes(), policy);
                System.out.println("sent successfully ");
                String responseStr = new String(response.getAllChunks(1000, TimeUnit.HOURS));
                System.out.println("response: "+responseStr);
            } finally {
                if (response!=null) {
                    response.close();
                }
            }
        } finally {
            if (dealer!=null) {
                dealer.close();
            }
        }
    }
}
