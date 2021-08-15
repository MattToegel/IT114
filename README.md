# Welcome to IT114 Repo
#### I recently started to reorganize all the content so it's easier to go through. 
#### There are still some things I need to work on, but for the most part the major items have been taken care of.
#### Instead of everything being in one branch I have divided up the projects/content into their own specific branches.

Right now you're here at the main branch (by default you all will have master branch).
This will be your hub or table of contents.
As of this writing, I still have Fall2019 Branch, Spring2020 Branch, and Master Branch (which is more or less Spring2020).

Here's the new table of contents:
- Class Items
  - ["Sockets - Client to Server"](https://github.com/MattToegel/IT114/tree/SocketSample_C2S)
  - ["Sockets - Client to Server - To Client](https://github.com/MattToegel/IT114/tree/SocketSample_C2S2C)
  - ["Sockets - Client to Server - To Multiple Clients](https://github.com/MattToegel/IT114/tree/SocketSample_C2S2MC)
  - ["Sockets - Console Chat Sample"](https://github.com/MattToegel/IT114/tree/SocketSample_Chat)
  - ["Sockets - Chat with Server Side Data Saving"](https://github.com/MattToegel/IT114/tree/SocketSample_Chat_ServerSaves)
  - ["Sockets - Button Clicker Sample"](https://github.com/MattToegel/IT114/tree/SocketSample_Btn_Clicker)
    - Shows game state synchronization and some custom UI
  - ["Sockets - Draw Player Sample"](https://github.com/MattToegel/IT114/tree/SocketSample_Draw_Players)
    - Shows how to sync painted players over the network (includes connect and disconnect syncing)
    - Shows basic sending of intent (only send direction if it changes) rather than constantly sending updates
      - Only the server "constantly" sends updates and it's at a slower frequency to adjust the client side prediction
      - Note that PlayerContainer is used both on client and server (the project keeps both lists in sync)
  - ["Generic UI Samples"](https://github.com/MattToegel/IT114/tree/GenericUISamples)
- Historical Items
  - ["Spring 2020 Project - WackoStacko"](https://github.com/MattToegel/IT114/tree/WackoStackoPhysics)
    - Check the Spring2020 Repo for the Non-physics based one that's deprecated
  - ["Fall2019 Project - TagGame"](https://github.com/MattToegel/IT114/tree/Fall2019-TagGame)
  - ["Fall2019 Project - Hectic RockPaperScissors"](https://github.com/MattToegel/IT114/tree/Fall2019-HecticRPS)


Test
