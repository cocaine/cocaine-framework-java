#!/bin/sh
java -Xloggc:./gc.log -Djava.library.path=./target/lib -cp target/classes cocaine.dealer.MainSingleThreaded python1/test_handle
