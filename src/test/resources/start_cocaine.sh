#!/bin/sh

prevdir=`pwd`
echo starting cocaine
LD_PRELOAD=/usr/lib/libperl.so \
cocained tcp://*:5000 --verbose -c $prevdir/src/test/resources/cocaine.conf