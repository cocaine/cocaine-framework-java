#!/bin/sh
echo java -Xloggc:./gc.log -Djava.library.path=./target/lib -cp target/classes cocaine.dealer.$1 
java -Xloggc:./gc.log -Djava.library.path=./target/lib -cp target/classes cocaine.dealer.$1
