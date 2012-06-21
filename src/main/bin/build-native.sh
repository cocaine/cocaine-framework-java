#!/bin/sh -e

case `uname` in
    Darwin) SHARED_FLAG=-dynamiclib ;;
    *) SHARED_FLAG=-shared ;;
esac

if [  ! -d "${JAVA_HOME}/include" ];
       then
             echo "$JAVA_HOME/include does not exist, try setting JAVA_HOME to point to jdk"
       exit 1;
fi

g++ -$SHARED_FLAG -o target/lib/libcocaine-framework-java.so -Wl,-soname,libcocaine-framework-java\
	-I/System/Library/Frameworks/JavaVM.framework/Versions/1.7/Headers \
	-Isrc/main/cpp  			 \
	-Itarget/generated 			 \
	-fpermissive -fPIC	 	     \
	-I${JAVA_HOME}/include       \
	-I${JAVA_HOME}/include/linux \
	 src/main/cpp/dealer.cpp  src/main/cpp/response.cpp src/main/cpp/util.cpp \
	-lcocaine-dealer -lc 
	
