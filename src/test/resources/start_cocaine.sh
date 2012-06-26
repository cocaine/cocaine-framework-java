#!/bin/sh

prevdir=`pwd`
echo starting cocaine
LD_PRELOAD=/usr/lib/libperl.so:/usr/lib/libpython2.7.so \
cocained tcp://*:5000 --verbose -c $prevdir/src/test/resources/cocaine.conf
