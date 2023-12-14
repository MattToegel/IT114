<table><tr><td> <em>Assignment: </em> IT114 Chatroom Milestone3</td></tr>
<tr><td> <em>Student: </em> Joshua DeMarco (jad237)</td></tr>
<tr><td> <em>Generated: </em> 12/14/2023 9:40:13 AM</td></tr>
<tr><td> <em>Grading Link: </em> <a rel="noreferrer noopener" href="https://learn.ethereallab.app/homework/IT114-003-F23/it114-chatroom-milestone3/grade/jad237" target="_blank">Grading</a></td></tr></table>
<table><tr><td> <em>Instructions: </em> <p>Implement the features from Milestone3 from the proposal document:&nbsp;&nbsp;<a href="https://docs.google.com/document/d/1ONmvEvel97GTFPGfVwwQC96xSsobbSbk56145XizQG4/view">https://docs.google.com/document/d/1ONmvEvel97GTFPGfVwwQC96xSsobbSbk56145XizQG4/view</a></p>
</td></tr></table>
<table><tr><td> <em>Deliverable 1: </em> Connection Screens </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add screenshots showing the screens with the following data</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T14.01.18image.png.webp?alt=media&token=39957665-b792-40df-bc80-38dcb436199f"/></td></tr>
<tr><td> <em>Caption:</em> <p>Host and port<br></p>
</td></tr>
<tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T14.01.37image.png.webp?alt=media&token=dd2d3124-6883-4d41-8ca1-c25fb31c177a"/></td></tr>
<tr><td> <em>Caption:</em> <p>Username<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Briefly explain the code for each step of the process</td></tr>
<tr><td> <em>Response:</em> <p>1 host is similar to what it was on the cli, however now<br>it uses a simple text input box from java swing that is able<br>to take the host, &quot;localhost&quot; or 127.0.0.1 as the input to connect the<br>client to the server<div>2 the port is once again similar and it has<br>the function of alsousing a text based box input from java swing, instead<br>the port is labeled as 3000, just as it was on the cli<br>ealier</div><div>3 the username aspect is the most changed as it it now ahs<br>a forward and back button if you typed something wrong or wanted to<br>chage the port or the host, the code works similar to as it<br>did prior to the upgrade to a GUI, but now it uses a<br>panel based system to move from card to card</div><br></p><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 2: </em> Chatroom view </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add screenshots showing the related UI</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T14.03.16image.png.webp?alt=media&token=64cab73d-94eb-48ef-aa49-b130dab02956"/></td></tr>
<tr><td> <em>Caption:</em> <p>3 clients with differing history<br></p>
</td></tr>
</table></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 3: </em> Chat Activities </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Show screenshots of the result of the following commands</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T14.04.14image.png.webp?alt=media&token=dbe32571-49b7-4f31-ad01-e836c5c12a35"/></td></tr>
<tr><td> <em>Caption:</em> <p>showcase of headsand tails<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Show the code snippets for each command</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T14.18.41image.png.webp?alt=media&token=3d13b601-8e06-4acd-9efc-c77cc983d507"/></td></tr>
<tr><td> <em>Caption:</em> <p>rolling in action<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 3: </em> Briefly explain the code flow of each command</td></tr>
<tr><td> <em>Response:</em> <p>Flip - simple boolean logic that takes the message of /flip sees the<br>command marker, and takes the command of flip, after this it enacts the<br>flip method and uses the sendmessage command to send out a formatted text<br>version of the result to the server, once it is received it is<br>then broadcasted out to all of the clients on the server<div>Roll - similar<br>excepts it is in multiple parts as it needs to seperate between the<br>&quot;d&quot; for 2d6 for example. this however stores multiple die results and posts<br>them as can be seen with the screenshot. it followz the same formatting<br>and server based rules as the flip</div><div>Formatting - the formatting cue came from<br>inside the &quot;view&quot; panel section where it is about to be broadcasted out<br>to eahc client however it updates the html tags with actual effects such<br>as bolding or color.</div><br></p><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 4: </em> Custom Text </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Screenshots of examples</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T14.05.46image.png.webp?alt=media&token=5d4c7749-88a7-4b99-be93-a31b2fe5070f"/></td></tr>
<tr><td> <em>Caption:</em> <p>all differing effects with some combinations, string was &quot;<em>hi</em> ~hi <em>hi</em> #blue# hi&quot;<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Briefly explain how you got the UI side to render the text accordingly</td></tr>
<tr><td> <em>Response:</em> <p>as slightly mentioned above in the view section, java swing offers a way<br>to show content in multple ways, orignally it was plain text however i<br>was able to edit the content that was dispalyed to accept html tags<br>and not show them as normal texts but effects. however this isn&#39;t meant<br>to deter one from using their own html tags as that is still<br>more than available for the user if they wish.<br></p><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 5: </em> Whisper </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add screenshots showing a demo of whisper commands</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T14.08.57image.png.webp?alt=media&token=e415f5d6-9be0-4437-b803-cc9dd23319c5"/></td></tr>
<tr><td> <em>Caption:</em> <p>JOSH client sending john a PM saying hi<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Show the server-side code snippets of how this feature works</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T14.19.50image.png.webp?alt=media&token=f328a793-7047-45fd-adb7-776bd21ec5ca"/></td></tr>
<tr><td> <em>Caption:</em> <p>pm function<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 3: </em> Briefly explain the code logic of how this was achieved</td></tr>
<tr><td> <em>Response:</em> <p>It checks if a message contains an @ symbol. If true, it extracts<br>the intended recipients and the message content. The recipients are determined by splitting<br>the message using @ and then further processing. The last recipient is separated<br>from the message content, and the sender&#39;s name is also included. Finally, a<br>function sendPrivateMessage is called to send the private message to the specified users.<br>The variable wasCommand is then set to true, indicating that a command was<br>processed.<br></p><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 6: </em> Mute/Unmute </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707834-bf5a5b13-ec36-4597-9741-aa830c195be2.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add screenshots demoing this feature</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T14.12.23image.png.webp?alt=media&token=e4c6970b-bc6c-40ee-9711-d439f197825c"/></td></tr>
<tr><td> <em>Caption:</em> (missing)</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Add screenshots of the code snippets that achieve this feature</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T14.20.56image.png.webp?alt=media&token=5e44f67f-b3e3-4130-aa0c-fd3ffbaa7be3"/></td></tr>
<tr><td> <em>Caption:</em> <p>mute in server thread<br></p>
</td></tr>
<tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T14.21.39image.png.webp?alt=media&token=446ccfbe-7d32-402e-b745-10baa43671fa"/></td></tr>
<tr><td> <em>Caption:</em> (missing)</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 3: </em> Briefly explain the code logic of how this was achieved</td></tr>
<tr><td> <em>Response:</em> <p>In the case of MUTE it extracts a list of users to be<br>muted from the message and mutes them if they are not already muted,<br>sending a confirmation private message, and adding them to a mute list in<br>the ServerThread.java file. Similarly, in the UNMUTE&quot;case, it extracts a list of users<br>to be unmuted, unmutes them if they are currently muted, and sends a<br>confirmation private message. In both cases, the variable wasCommand is set to true<br>to indicate that a command was processed.<br></p><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 7: </em> Misc </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707795-a9c94a71-7871-4572-bfae-ad636f8f8474.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Pull request from milestone3 to main</td></tr>
<tr><td>Not provided</td></tr>
</table></td></tr>
<table><tr><td><em>Grading Link: </em><a rel="noreferrer noopener" href="https://learn.ethereallab.app/homework/IT114-003-F23/it114-chatroom-milestone3/grade/jad237" target="_blank">Grading</a></td></tr></table>