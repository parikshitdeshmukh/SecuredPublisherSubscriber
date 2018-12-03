#!/usr/bin/env python

import socket, subprocess, time

pubs = ['10.142.0.3','10.142.0.5','10.142.0.7','10.142.0.9']
subs = ['10.142.0.4', '10.142.0.6','10.142.0.8','10.142.0.10']

def run():
	if socket.gethostbyname(socket.gethostname()) in pubs:
		subprocess.Popen(['java', '-cp', '/home/parikshitd92/PubSub_Client.jar','Client.ClientTest_NodeFailure_Backlog','1']);
	elif socket.gethostbyname(socket.gethostname()) == "10.142.0.4":
		subprocess.Popen(['java', '-cp', '/home/parikshitd92/PubSub_Client.jar','Client.ClientTest_NodeFailure_Backlog','0']);
	else:
		subprocess.Popen(['java', '-cp', '/home/parikshitd92/PubSub_Client.jar','Client.ClientTest_NodeFailure_Backlog','2']);
if __name__ == "__main__":
	run()