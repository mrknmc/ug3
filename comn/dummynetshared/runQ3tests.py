import subprocess
import time
import re
address = "localhost"
port = "6000"
filename = "testfile.jpg"
retrytimeout = "40"

packetloss = "0.005"
bandwidth = "10Mbits/s"

# Set up ipfw pipes
subprocess.call("ipfw flush")
subprocess.call("ipfw add pipe 100 in")
subprocess.call("ipfw add pipe 200 out")

# Loop over window sizes
for i in range(9):
    windowSize = 2 ** i
    print "WindowSize = %d | " % windowSize,

    # Loop over delays
    for delay in [10, 100, 3000]:
        # Configure ipfw pipes        
        subprocess.call("ipfw pipe 100 config delay %sms plr %s bw %s" % (delay, packetloss, bandwidth))
        subprocess.call("ipfw pipe 200 config delay %sms plr %s bw %s" % (delay, packetloss, bandwidth))

        # Get sender and receiver commands
        receiverCommand = "java Receiver3 " + port + " received" + filename
        senderCommand = ("java Sender3 %s %s %s %s %d" %
                         (address, port, filename, retrytimeout, windowSize))

        # Run Sender and Receiver
        subprocess.Popen(receiverCommand)
        time.sleep(0.4) # wait for receiver to set up

        result = subprocess.Popen(senderCommand, stdout=subprocess.PIPE).communicate()[0]

        # Print the results
        print re.search("Throughput: \((.*)\)", result).group(1) + " ",
        time.sleep(0.4)

    print ""
