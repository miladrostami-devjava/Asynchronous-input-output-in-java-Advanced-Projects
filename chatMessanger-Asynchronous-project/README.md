# Asynchronous chat use of Asynchronous i/o in java
Suppose you are developing a chat application that needs to process
a large number of clients simultaneously.
Instead of using blocked threads for each client, 
we use asynchronous programming to perform I/O operations optimally and efficiently.

Project design:
The server must be able to accept connections asynchronously.
Clients connect to the server and send messages simultaneously.
Each client receives and displays incoming messages from the server.