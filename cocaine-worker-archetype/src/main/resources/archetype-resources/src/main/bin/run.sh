#!/bin/sh

java -Dlog4j.configuration=log4j.properties -jar ${artifactId}-${version}.jar -cp ./lib $@
