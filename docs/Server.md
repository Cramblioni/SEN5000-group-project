# Server

## General flow

For the main `Server` class:
1) Gets the provided address from the user (defaulting to `localhost:13337`)
2) Binds a socket to that address and starts listening for connections
3a) For each connection, it creates a handler and passes that to a thread pool
3b) **IF** the thread pool is full, then it informs the client and closes the
    connection.

For the private `ClientHandler`:
1) Notifies the client of a successful connection.
2) Recieves the client's message
3) Timestamps the message
4) Obtains the File-Write lock
5) Write's the clients message to the `.csv` file
6) Closes the connection

## Messaging format

The server informs the client of the connection's success via sending "NO" (78,
79) or "OK" (79, 75). "NO" represents a rejected connection, and the socket is
closed on the server side. "OK" represents an accepted connection and the server
waits for the client to send it's reading message.
