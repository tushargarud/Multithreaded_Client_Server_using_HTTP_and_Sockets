A Chat Room System

This is a java swing-based chat room system project. The chat room server communicates with its clients using HTTP protocol and sockets. Clients use HTTP GET requests to poll the server and HTTP POST requests to post messages to the server. Both client and server are multithreaded. The server also saves the messages in a MySQL database and loads them back when it starts.

To run the project:
1)	Create a table named ‘messages’ in your MySQL database using the file ‘http_socket_db.sql’ provided in the zip.
2)	Update the connection string in connectToMySql() method of ‘HTTPMultithreadedSocketServer.java’.
3)	Include the library ‘mysql-connector-java-5.1.45-bin.jar’ in your classpath. It is a JDBC library for MySQL connectivity.
4)	Start the server process by running ‘HTTPMultithreadedSocketServer.java’.
5)	Start one or more client processes by running ‘HTTPMultithreadedSocketClient.java’.
6)	Now the clients can send messages to the server.
7)	To disconnect the client, send a message as ‘exit’.
