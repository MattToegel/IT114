*Rock Paper Scissors Multi Client + Server*
Note: Ignore mention of tic-tac-toe, I had the wrong game in mind when I was typing and I didn't go back and fix the commit comments.
1. Download the client ``builds\RPSClient.jar``
2. Set host/port settings in the top text fields and click connect
	1. If the server is up you can use the following conneciton details:
	
		1.host: sandbox.ethereallab.app
		2.port: 3100
	2. If there's an error, the text fields will show again with the error. Connection refused means there's no server currently listening.
3. If the server is up you'll automatically connect.
4. Choose Rock/Paper/Scissors
	1. You will either be matched with an existing random opponent that has made a choice already.
	2. Or you'll create a game that'll be waiting for an opponent.
	3. You can click other choices to try to randomly match with another opponent.
5. You'll see the statuses of each completed game during your session as well as when a client connects or disconnects.
6. Each instance of a game is separate and you may be matched up with different clients each time you make a decision.
