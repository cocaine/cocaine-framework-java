#!/bin/sh -e

prevdir=`pwd`
cd src/test/resources
tar czf package.tar.gz test.pl
cd $prevdir
LD_PRELOAD=/usr/lib/libperl.so:/usr/lib/libpython2.7.so cocaine-deploy \
	-m $prevdir/src/test/resources/perl_test1.app.manifest \
	-p $prevdir/src/test/resources/package.tar.gz perl_test1 \
	-c $prevdir/src/test/resources/cocaine.conf 
rm -rf ./target/data/cache/
rm -rf src/test/resources/package.tar.gz