PROJECT (cocaine-framework-java Java)
CMAKE_MINIMUM_REQUIRED(VERSION 2.8.6)

SET(DEALER_VERSION 0.9.7)

INCLUDE(cmake/locate_library.cmake)
FIND_PACKAGE(Java)
FIND_PACKAGE(JNI)
INCLUDE(UseJava)
SET (CMAKE_JAVA_TARGET_VERSION ${DEALER_VERSION})
SET (SRC
	src/main/java/cocaine/dealer/ArrayHolder.java
    src/main/java/cocaine/dealer/DealerImpl.java
    src/main/java/cocaine/dealer/Dealer.java
	src/main/java/cocaine/dealer/Library.java
	src/main/java/cocaine/dealer/Message.java
	src/main/java/cocaine/dealer/MessagePolicy.java
	src/main/java/cocaine/dealer/Ptr.java
	src/main/java/cocaine/dealer/Response.java
	src/main/java/cocaine/dealer/exceptions/AppException.java)
ADD_JAR(cocaine-framework-java ${SRC})



GET_PROPERTY(JARFILE TARGET cocaine-framework-java PROPERTY JAR_FILE)

PROJECT(cocaine-framework-java-lib)

LOCATE_LIBRARY(LIBCOCAINEDEALER "cocaine/dealer/dealer.hpp" "cocaine-dealer")

INCLUDE_DIRECTORIES(
	${LIBCOCAINEDEALER_INCLUDE_DIRS}
	target/generated
	${JAVA_INCLUDE_PATH}
	${JAVA_INCLUDE_PATH2}
)

LINK_DIRECTORIES(
	${LIBCOCAINE_DEALER_LIBRARY_DIRS})
FILE(GLOB CPP_SOURCES
    "src/main/cpp/*.cpp")

ADD_CUSTOM_COMMAND(
	 OUTPUT target/generated/cocaine_dealer_DealerImpl.h target/generated/cocaine_dealer_Response.h
	 COMMAND ${Java_JAVAH_EXECUTABLE} -jni -d target/generated -classpath ${JARFILE} cocaine.dealer.Dealer cocaine.dealer.Response
         WORKING_DIRECTORY 
         COMMENT "generating jni header files"
         )

ADD_LIBRARY(cocaine-framework-java-lib SHARED
    target/generated/cocaine_dealer_DealerImpl.h
    target/generated/cocaine_dealer_Response.h
    ${CPP_SOURCES}
)

TARGET_LINK_LIBRARIES(cocaine-framework-java-lib
    cocaine-dealer
)
SET_TARGET_PROPERTIES(cocaine-framework-java-lib PROPERTIES
    VERSION ${DEALER_VERSION}
    OUTPUT_NAME "cocaine-framework-java")


INSTALL_JAR(cocaine-framework-java share/java)

INSTALL(
    TARGETS
        cocaine-framework-java-lib
    LIBRARY DESTINATION lib/jni COMPONENT runtime)
