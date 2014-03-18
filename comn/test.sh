#!/usr/bin/env bash

# Question 1
# 10% packet loss rate 10Mbps bandwidth and 10ms delay

ipfw flush
ipfw add pipe 100 in
ipfw add pipe 200 out

ipfw pipe 100 config delay 10ms plr 0.05 bw 10Mbits/s
ipfw pipe 200 config delay 10ms plr 0.05 bw 10Mbits/s

echo timeout, retransmission, throughput

for TIMEOUT in 10 20 30 40 50 60 70 80 90 100
do
        echo -n "$TIMEOUT, "
        java Receiver2 54321 rfile.jpg > /dev/null &
        sleep 1
        java Sender2 localhost 54321 sfile.jpg $TIMEOUT
        wait
done

# Question 3
# 1% packet loss rate 10Mbps bandwith and variable delay

echo window, delay, throughput

ipfw flush
ipfw add pipe 100 in
ipfw add pipe 200 out

for DELAY in 10 100 300
do
        ipfw pipe 100 config delay ${DELAY}ms plr 0.005 bw 10Mbits/s
        ipfw pipe 200 config delay ${DELAY}ms plr 0.005 bw 10Mbits/s

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

ipfw flush
ipfw add pipe 100 in
ipfw add pipe 200 out

ipfw pipe 100 config delay 100ms plr 0.005 bw 10Mbits/s
ipfw pipe 200 config delay 100ms plr 0.005 bw 10Mbits/s

echo window, throughput

for WINDOW in 8 16 32 64 128 256
do
        echo -n "$WINDOW, "
        java Receiver4 54321 rfile.jpg > /dev/null &
        sleep 1
        java Sender4 localhost 54321 sfile.jpg  $WINDOW
done
