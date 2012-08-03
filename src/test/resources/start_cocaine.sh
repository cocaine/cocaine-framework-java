#!/bin/sh

prevdir=`pwd`
echo starting cocaine
cocained tcp://*:5000 --verbose -c $prevdir/src/test/resources/cocaine.conf
