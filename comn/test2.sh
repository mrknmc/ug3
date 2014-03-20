#!/usr/bin/env bash

# Question 3
# 1% packet loss rate 10Mbps bandwith and variable delay

control_c() {
    killall -9 java
    exit
}

echo window, delay, throughput

ipfw -f flush > /dev/null
ipfw add pipe 100 in > /dev/null
ipfw add pipe 200 out > /dev/null

for DELAY in 10 100 300
do
        ipfw pipe 100 config delay ${DELAY}ms plr 0.005 bw 10Mbits/s > /dev/null
        ipfw pipe 200 config delay ${DELAY}ms plr 0.005 bw 10Mbits/s > /dev/null

        # loop through windows
        for WINDOW in 1 2 4 8 16 32 64 128 256
        do
                echo -n "$WINDOW, $DELAY, "
                java Receiver3 54321 rfile.jpg > /dev/null & rec_pid=$!
                sleep 1
                java Sender3 localhost 54321 sfile.jpg $TIMEOUT $WINDOW | xargs echo "$TIMEOUT, "
                # kill the receiver
                kill -9 $rec_pid > /dev/null
                wait $rec_pid > /dev/null
        done
done
