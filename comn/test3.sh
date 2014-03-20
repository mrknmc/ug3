#!/usr/bin/env bash

# Question 5
# 1% packet loss rate 10Mbps bandwidth and 100ms delay

control_c() {
    killall -9 java
    exit
}

ipfw -f flush > /dev/null
ipfw add pipe 100 in > /dev/null
ipfw add pipe 200 out > /dev/null

ipfw pipe 100 config delay 100ms plr 0.005 bw 10Mbits/s > /dev/null
ipfw pipe 200 config delay 100ms plr 0.005 bw 10Mbits/s > /dev/null

echo window, throughput

for WINDOW in 8 16 32 64 128 256
do
        echo -n "$WINDOW, "
        java Receiver4 54321 rfile.jpg > /dev/null &
        sleep 1
        java Sender4 localhost 54321 sfile.jpg  $WINDOW
done