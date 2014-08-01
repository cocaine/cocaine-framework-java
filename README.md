# Cocaine Framework Java

## Packages
 * Cocaine Core
 * Cocaine Client
 * Cocaine Client Archetype
 * Cocaine Worker
 * Cocaine Worker Archetype
 * Cocaine Services

## Building Cocaine Framework Java with Maven

### What you’ll need
 * Latest stable [Oracle JDK 7](http://www.oracle.com/technetwork/java "Oracle JDK 7")
 * Latest stable [Apache Maven](http://maven.apache.org "Apache Maven")

### Build Java code
You can execute several build lifecycle goals with Maven, including goals to compile the project’s code,
 create a library package (such as a JAR file), install the library in the local Maven dependency repository
  and pack sources.

To try out the build, issue the following at the command line:

    mvn compile

This will run Maven, telling it to execute the compile goal. When it’s finished, you should find
 the compiled .class files in the <artifactId>/target/classes directory.

Since it’s unlikely that you’ll want to distribute or work with .class files directly,
 you’ll probably want to run the package goal instead:

    mvn package

The package goal will compile your Java code, run any tests, and finish by packaging the code up
 in a JAR file within the target directory. The name of the JAR file will be based on the project’s <version>.
  For example, the JAR files will be named cocaine-core-0.10.5-1.jar and cocaine-services-0.10.5-1.jar.

Maven also maintains a repository of dependencies on your local machine (usually in a ~/.m2/repository directory)
 for quick access to project dependencies. If you’d like to install JAR files to that local repository,
  then you should invoke the install goal:

    mvn install

The install goal will compile, test, and package code and then copy it into the local dependency repository,
 ready for another project to reference it as a dependency.

You can also run the source:jar goal:

    mvn source:jar

The source:jar goal will package the sources up in a JAR file within the target directory.
 The name of the JAR file will be based on the project’s <version>.
  For example, the JAR file will be named cocaine-core-0.10.5-1-sources.jar and cocaine-services-0.10.5-1-sources.jar.
