#!/bin/sh -e

case `uname` in
    Darwin) SHARED_FLAG=-dynamiclib ;;
    *) SHARED_FLAG=-shared ;;
esac

JAVAINCDIR="${JAVA_HOME}/include";
if [  ! -d "$JAVAINCDIR" ]; then
    echo "$JAVAINCDIR does not exist, try setting JAVA_HOME to point to jdk"
    exit 1;
fi

DEBUG_FLAG=""
if [ "${CFJ_DEBUG+x}" = "x" ] ; then 
    echo "building in debug mode"
    DEBUG_FLAG="-g"
    echo "DEBUG_FLAG is $DEBUG_FLAG"
fi
g++ -$SHARED_FLAG $DEBUG_FLAG -o target/lib/libcocaine-framework-java.so -Wl,-soname,libcocaine-framework-java\
	-I/System/Library/Frameworks/JavaVM.framework/Versions/1.7/Headers \
	-Isrc/main/cpp  			 \
	-Itarget/generated 			 \
	-fpermissive -fPIC	 	     \
	-I${JAVA_HOME}/include       \
	-I${JAVA_HOME}/include/linux \
	 src/main/cpp/dealer.cpp  src/main/cpp/response.cpp src/main/cpp/util.cpp \
	-lcocaine-dealer -lc 
	
