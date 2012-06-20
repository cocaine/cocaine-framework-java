#!/bin/sh
g++ -shared -o target/lib/libcocaine-framework-java.so -Wl,-soname,libcocaine-framework-java\
	-I${JAVA_HOME}/include \
	-I${JAVA_HOME}/include/linux \
	-I/usr/local/include 	\
	-I/usr/include	\
	-Isrc/main/cpp/include  \
	-Itarget/generated 	\
	-fpermissive -fPIC	 	\
	 src/main/cpp/impl/client.cpp  src/main/cpp/impl/response.cpp src/main/cpp/impl/util.cpp\
	-L/usr/local/lib	\
	-lcocaine-dealer -lc \
	
