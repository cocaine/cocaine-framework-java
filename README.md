Installation
====================

install cocaine-core (libcocaine-dealer is bundled into it)
http://wiki.yandex-team.ru/Cocaine/Deployment

install cocaine-plugins (https://github.com/cocaine/cocaine-plugins)

make sure $JAVA_HOME points to jdk

cd to cocaine-framework-java root dir
$> ant

Example
====================
```java
 @Test
    public void testGood() throws TimeoutException {
        Client client = null;
        String testString = "hello_world";
        try {
            client = new Client("./src/test/resources/dealer_config.json");
            Response response = null;
            try {
                response = client.sendMessage("app1/test_handle",
                        new TextMessage(testString), policy);
                String responseStr = response.get(TIMEOUT, TIME_UNIT);
                Assert.assertContains(responseStr, testString);
                response.close();
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }
```

Cocaine app
====================

there is a sample cocaine app bundled.
To deploy and run it on a new cocaine instance run 
src/test/resources/run.sh

the app itself is a perl script : srt/test/resources/test.pl
with the manifest srt/test/resources/perl_test1.app.manifers
2 handles are defined:
test_handle - echoes the request back
test_handle_timeout - never returns - sleeps forever

Tests
====================

there are 2 functional and 1 performance test
in src/test/java 
they depend on Cocaine app running in cocaine

when running them make sure LD_LIBRARY_PATH contains target/lib

Shortcomings
====================

1) Response.get does not throw Timeout exception if it was the get itself who timed out, not the server timeout
specified in MessagePolicy

2) No support for msgpack, only text messages currently. 
Python cocaine framework supports of packing app responses with msgpack.
so it should be possible to unpack them on java side (?)
 
