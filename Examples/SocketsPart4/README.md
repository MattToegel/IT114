Sockets Part 4 Converts Sockets Part 3 to use a "Payload" object instead of Strings.

This version's client also differs, instead of passing port as an argument you need to now pass host:port.

This sample still uses a String message but it's encapsulated in a Payload object so we can attach other data to it if necessary.
