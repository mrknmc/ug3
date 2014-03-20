#!/usr/bin/env bash

# Question 1
# 10% packet loss rate 10Mbps bandwidth and 10ms delay

control_c() {
	killall -9 java
	exit
}

trap control_c SIGINT

ipfw -f flush > /dev/null
ipfw add pipe 100 in > /dev/null
ipfw add pipe 200 out > /dev/null

ipfw pipe 100 config delay 10ms plr 0.05 bw 10Mbits/s > /dev/null
ipfw pipe 200 config delay 10ms plr 0.05 bw 10Mbits/s > /dev/null

echo timeout, retransmission, throughput

for TIMEOUT in `seq 10 10 100`
do
        java Receiver2 54321 rfile.jpg > /dev/null & rec_pid=$!
        sleep 1
        java Sender2 localhost 54321 sfile.jpg $TIMEOUT | xargs echo "$TIMEOUT, "
        # kill the receiver
        kill -9 $rec_pid > /dev/null
        wait $rec_pid > /dev/null
done
