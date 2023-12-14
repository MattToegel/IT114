<table><tr><td> <em>Assignment: </em> IT114 Chatroom Milestone4</td></tr>
<tr><td> <em>Student: </em> Joshua DeMarco (jad237)</td></tr>
<tr><td> <em>Generated: </em> 12/14/2023 6:46:37 PM</td></tr>
<tr><td> <em>Grading Link: </em> <a rel="noreferrer noopener" href="https://learn.ethereallab.app/homework/IT114-003-F23/it114-chatroom-milestone4/grade/jad237" target="_blank">Grading</a></td></tr></table>
<table><tr><td> <em>Instructions: </em> <p>Implement the features from Milestone3 from the proposal document:&nbsp;&nbsp;<a href="https://docs.google.com/document/d/1ONmvEvel97GTFPGfVwwQC96xSsobbSbk56145XizQG4/view">https://docs.google.com/document/d/1ONmvEvel97GTFPGfVwwQC96xSsobbSbk56145XizQG4/view</a></p>
</td></tr></table>
<table><tr><td> <em>Deliverable 1: </em> Client can export chat history of their current session (client-side) </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add screenshot of related UI</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T23.25.03image.png.webp?alt=media&token=7e30f543-675c-4d2f-b964-2cd011af5dd6"/></td></tr>
<tr><td> <em>Caption:</em> <p>Code in Client UI, could not get the button feature working but this<br>how it should properly work<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Add screenshot of exported data</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T23.46.24image.png.webp?alt=media&token=4247b1fc-ef98-4c47-8799-3ff913a1f580"/></td></tr>
<tr><td> <em>Caption:</em> <p>Could not figure out data export :(<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 3: </em> Briefly explain how you implemented this</td></tr>
<tr><td> <em>Response:</em> <p>Essentially, it is a button with an action trigger attached with it, on<br>pressing this button it would call an export method that would iterate through<br>the chat history line by line and adding that to a newly created<br>text file. It would copy down the plain text as it wouldn&#39;t show<br>the html tags<br></p><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 2: </em> Client's mute list will persist across sessions (server-side) </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add a screenshot of how the mute list is stored</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T23.31.14image.png.webp?alt=media&token=76a30b42-4ad5-4df4-9e8f-824eccce2ab0"/></td></tr>
<tr><td> <em>Caption:</em> <p>mute list for indiviudal &quot;josh&quot; in josh.txt showingcasing user john and sam as<br>being muted<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Add a screenshot of the code saving/loading mute list</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T23.32.18image.png.webp?alt=media&token=deea098a-28de-4b59-b7aa-b44009b75906"/></td></tr>
<tr><td> <em>Caption:</em> <p>save, load mutelist<br></p>
</td></tr>
<tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T23.32.41image.png.webp?alt=media&token=0be31c2b-13fe-4495-863c-e43c4187ae70"/></td></tr>
<tr><td> <em>Caption:</em> <p>sync func<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 3: </em> Briefly explain how you implemented this</td></tr>
<tr><td> <em>Response:</em> <div>saveMuteList() -This method creates a text file named after the client, containing a<br>string concatenating the client's name and muted clients, separated by commas. It handles<br>file writing operations, catching and printing stack traces in case of IOException.</div><div><br></div><div>loadMuteList() -<br>Responsible for loading muted client data from a text file named after the<br>client, it reads and processes the file content, then mutes each client found<br>by calling an external mute method, printing sync for each. If any file-related<br>exceptions occur, the corresponding stack trace is printed.</div><div><br></div><div>syncIsMuted() -Part of a communication system,<br>this method constructs a payload indicating whether a client should be muted or<br>unmuted, sends it, and returns a boolean indicating the success of the payload<br>sending process.<br></div><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 3: </em> Client's will receive a message when they get muted/unmuted by another user </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add a screenshot showing the related chat messages</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T23.39.11image.png.webp?alt=media&token=d39217b2-a598-44fb-a102-3cebdac5f69b"/></td></tr>
<tr><td> <em>Caption:</em> <p>a muting b<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Add a screenshot of the related code snippets</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T23.40.01image.png.webp?alt=media&token=c83604b7-bc9d-491c-98ba-b66e42358870"/></td></tr>
<tr><td> <em>Caption:</em> <p>Message of Type Mute on the ServerSide<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 3: </em> Briefly explain how you implemented this</td></tr>
<tr><td> <em>Response:</em> <p>I used a getClientName() in ServerThread.java to be able to go and ge<br>tthe clients name for the pm mute message, after, I used the sendPrivateMessage()<br>feature in order to send a prviate message to the second user. all<br>of this is handled server side.&nbsp;<br></p><br></td></tr>
</table></td></tr>
<table><tr><td> <em>Deliverable 4: </em> User list should update per the status of each user </td></tr><tr><td><em>Status: </em> <img width="100" height="20" src="https://user-images.githubusercontent.com/54863474/211707773-e6aef7cb-d5b2-4053-bbb1-b09fc609041e.png"></td></tr>
<tr><td><table><tr><td> <em>Sub-Task 1: </em> Add screenshot for Muted users by the client should appear grayed out</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T23.43.09image.png.webp?alt=media&token=c413c9c4-5f0b-4108-b1eb-d82914ad0056"/></td></tr>
<tr><td> <em>Caption:</em> <p>a and b mute list after muting each other<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 2: </em> Add screenshot for Last person to send a message gets highlighted</td></tr>
<tr><td><table><tr><td><img width="768px" src="https://firebasestorage.googleapis.com/v0/b/learn-e1de9.appspot.com/o/assignments%2Fjad237%2F2023-12-14T23.45.45image.png.webp?alt=media&token=28f7457f-16f4-4ffa-adf4-4d5e9d9e2f0b"/></td></tr>
<tr><td> <em>Caption:</em> <p>Couldn&#39;t figure out implmentation for this<br></p>
</td></tr>
</table></td></tr>
<tr><td> <em>Sub-Task 3: </em> Briefly explain how you implemented this</td></tr>
<tr><td> <em>Response:</em> <p>I would imagine this would be implemented through using the last known message<br>and setting the name of the in UserListPanel.java, however I could not figure<br>this feature out as I had trouble with UI elements just no longer<br>updating while working on this.<br></p><br></td></tr>
</table></td></tr>
<table><tr><td><em>Grading Link: </em><a rel="noreferrer noopener" href="https://learn.ethereallab.app/homework/IT114-003-F23/it114-chatroom-milestone4/grade/jad237" target="_blank">Grading</a></td></tr></table>