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

public class MainSingleThreadedChunked {
    private final static String CONFIG_PATH = "./src/test/resources/dealer_config.json";
    private final static String PATH = "python1/test_handle_chunked";

    public static void main(String[] args) throws Exception {
        byte[] message = TestHelper.getLargeMessage();
        MessagePolicy messagePolicy = MessagePolicy.builder()
                .maxRetries(10)
                .timeout(100000, TimeUnit.MILLISECONDS).build();
        Dealer dealer = null;
        try {
            dealer = new Dealer(CONFIG_PATH);
            TestHelper.timeIt(TestHelper.responseGetStringCallable(dealer, PATH, message, messagePolicy));
        } finally {
            if (dealer != null) {
                dealer.close();
            }
        }
    }
}
