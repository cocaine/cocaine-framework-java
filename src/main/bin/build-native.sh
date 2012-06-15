#!/bin/sh

gcc -o libcocaineframeworkjava.so -shared -Wl,-soname,libcocaineframeworkjava.so \
	-I${JAVA_HOME}/include \
	-I${JAVA_HOME}/include/linux \
	-I/usr/local/include \
	-Isrc/main/cpp/include \
	-Itarget/generated \
	-fpermissive -fPIC \
	 src/main/cpp/impl/client.cpp -lc
