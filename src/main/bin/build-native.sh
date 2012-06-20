#!/bin/sh
g++ -shared -o target/lib/libcocaine-framework-java.so -Wl,-soname,libcocaine-framework-java\
	-I${JAVA_HOME}/include \
	-I${JAVA_HOME}/include/linux \
	-Isrc/main/cpp  \
	-Itarget/generated 	\
	-fpermissive -fPIC	 	\
	 src/main/cpp/client.cpp  src/main/cpp/response.cpp src/main/cpp/util.cpp\
	-lcocaine-dealer -lc 
	
