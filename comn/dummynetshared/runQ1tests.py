import subprocess
import time
import re
address = "localhost"
port = "6000"
sfile = "sfile.jpg"
rfile = "rfile.jpg"

# Loop over timeouts
for retrytimeout in range(10, 101, 10):
    senderCommand = "java Sender2 " + address + " " + port + " " + sfile + " " + str(retrytimeout)
    receiverCommand = "java Receiver2 " + port + " " + rfile

    print receiverCommand

    # Run receiver and sender
    subprocess.Popen(receiverCommand)
    result = subprocess.Popen(senderCommand, stdout=subprocess.PIPE).communicate()[0]

    # Get results
    matches = re.search("Retransmissions: \((.*?)\).*Throughput: \((.*?)\)", result)
    print matches.group(1) + " | " + matches.group(2)
    
    time.sleep(0.3)
