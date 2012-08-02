#!/bin/sh -e

prevdir=`pwd`
cd src/test/resources
tar czf perl_package.tar.gz test.pl
tar czf python_package.tar.gz __init__.py
cd $prevdir

rm -rf ./target/cocaine/data

cocaine-deploy \
    -m $prevdir/src/test/resources/perl_test1.app.manifest \
    -p $prevdir/src/test/resources/perl_package.tar.gz perl_test1 \
    -c $prevdir/src/test/resources/cocaine.conf 

cocaine-deploy \
    -m $prevdir/src/test/resources/python_test1.app.manifest \
    -p $prevdir/src/test/resources/python_package.tar.gz python_test1 \
    -c $prevdir/src/test/resources/cocaine.conf 

rm -rf src/test/resources/perl_package.tar.gz
rm -rf src/test/resources/python_package.tar.gz
