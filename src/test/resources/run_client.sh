#!/bin/sh
echo java -Xloggc:./gc.log -Djava.library.path=./target/lib -cp target/classes cocaine.dealer."$@"
java -Xloggc:./gc.log -Djava.library.path=./target/lib -cp target/classes cocaine.dealer."$@" 
