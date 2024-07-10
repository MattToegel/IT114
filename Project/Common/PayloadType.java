package Project.Common;

public enum PayloadType {
    CLIENT_CONNECT, // client requesting to connect to server (passing of initialization data [name])
    CLIENT_ID,  // server sending client id
    SYNC_CLIENT,  // silent syncing of clients in room
    DISCONNECT,  // distinct disconnect action
    ROOM_CREATE,
    ROOM_JOIN, // join/leave room based on boolean
    MESSAGE, // sender and message,
    ROOM_LIST, // client: query for rooms, server: result of query,
    READY, // client to trigger themselves as ready, server to sync the related status of a particular client
    SYNC_READY, // quiet version of READY, used to sync existing ready status of clients in a GameRoom
    RESET_READY, // trigger to tell the client to reset their whole local list's ready status (saves network requests)
    PHASE, // syncs current phase of session (used as a switch to only allow certain logic to execute)
    MOVE, // syncs as a point/coordinate
    GRID_DIMENSION, // syncs grid dimension for server-side controlled grid building
    TURN, // used for syncing turn data
    CARDS_IN_HAND, //syncs a full hand to a client
    ADD_CARD, // syncs a new card to a client's hand
    REMOVE_CARD, // client to tell server-side it'll discard, server-side confirms to client
    USE_CARD, // client to tell server-side it'll activate a card, server-side confirms to client
    TOWER_PLACE, // client to tell the server-side it wants to place a Tower at a coordinate
    TOWER_ATTACK, // client to tell the server-side it wants to attack with a Tower at x,y and hit specific IDs
    TOWER_STATUS, // server-side to tell the Clients to sync Tower data
    TOWER_ALLOCATE, // client-side to tell the server-side to alloc/dealloc energy
    ENERGY, // server-side to update the Clients of their energy
    END_TURN // client-side to tell the server-side that they're ending their turn
}
