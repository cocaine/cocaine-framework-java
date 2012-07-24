echo runnint perftest_$1
src/test/resources/run_client.sh $1 &>perftest_$1.log &
echo src/test/resources/run_client.sh $1
sleep 4s
perfpid="`greppid dealer.$1`"
echo perfpid_$1=$perfpid
echo running jstat
jstat -gc -h10 $perfpid 20000 999999 > jstat_$1.log &
echo running top_$1
top -b -p $perfpid > top_$1.log &
