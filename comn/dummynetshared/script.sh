# Question 1
# 10% packet loss rate 10Mbps bandwidth and 10ms delay

ipfw flush
ipfw add pipe 100 in
ipfw add pipe 200 out

ipfw pipe 100 config delay 10ms plr 0.05 bw 10Mbits/s
ipfw pipe 200 config delay 10ms plr 0.05 bw 10Mbits/s 

for retrytimeout in 10 20 30 40 50 60 70 80 90 100
do
	echo Timeout $retrytimeout
	echo retransmission, throughput
	java Receiver2 54321 rfile.jpg > /dev/null &
	sleep 1
	java Sender2 localhost 54321 sfile.jpg $retrytimeout
	wait
done

# Question 3

echo window, delay, retransmission, throughput

for delay in 10 100 300
do
	# 1% packet loss 10Mbps bandwith and variable delay
	ipfw flush
	ipfw add pipe 100 in
	ipfw add pipe 200 out
	ipfw pipe 100 config delay ${delay}ms plr 0.005 bw 10Mbits/s
	ipfw pipe 200 config delay ${delay}ms plr 0.005 bw 10Mbits/s
	
	# loop through windows
	for window in 1 2 4 8 16 32 64 128 256
	do
		echo -n $window, $delay, 
		java Receiver3 54321 rfile.jpg > /dev/null &
		sleep 1
		java Sender3 localhost 54321 sfile.jpg &retrytimeout $window
	done
done
