# Welcome to Wacko Stacko

This is a game I'm working on with my IT114 class of the Spring 2020 semester.

By the end of the project this is the expected outcome:
- Players can join the live server
- Players can pick a display name
- During the game players can move with WASD or Arrows and the Space bar to jump
- This is a physics based game where players can utilize other players and the environment to achieve the game goal
- The game goal is to be the first to reach the top of the game board by jumping on top of environment objects and/or players to cross the "finish line"
- When a player crosses the line the game will reset for all connected players and begin a new round
- Players should be able to join/leave at will
- The server will save scores similar to old Arcade style where the session's Name is saved with the Score upon leaving

You can refer to this project proposal for more details [here](https://docs.google.com/document/d/1BGen4Q-Fj6U8vxke168AlggUg3jn8kmfgUgz_r6rQik/edit)

Display names are filted through a blacklist pulled from https://www.freewebheaders.com/bad-words-list-and-page-moderation-words-list-for-facebook/.
In my sample, any detected blacklisted word will get replaces with a random "silly" word (just for giggles).
