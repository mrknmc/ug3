#!/usr/bin/env bash

# Question 1
# 10% packet loss rate 10Mbps bandwidth and 10ms delay

ipfw -f flush > /dev/null
ipfw add pipe 100 in > /dev/null
ipfw add pipe 200 out > /dev/null

ipfw pipe 100 config delay 10ms plr 0.05 bw 10Mbits/s > /dev/null
ipfw pipe 200 config delay 10ms plr 0.05 bw 10Mbits/s > /dev/null

echo timeout, retransmission, throughput

for TIMEOUT in 10 20 30 40 50 60 70 80 90 100
do
        echo -n "$TIMEOUT, "
        java Receiver2 54321 rfile.jpg > /dev/null &
        sleep 1
        java Sender2 localhost 54321 sfile.jpg $TIMEOUT
        wait
done

echo

# Question 3
# 1% packet loss rate 10Mbps bandwith and variable delay

echo window, delay, throughput

ipfw - flush > /dev/null
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
                java Receiver3 54321 rfile.jpg > /dev/null &
                sleep 1
                java Sender3 localhost 54321 sfile.jpg $TIMEOUT $WINDOW
        done
done

# Question 5
# 1% packet loss rate 10Mbps bandwidth and 100ms delay

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
