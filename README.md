# IDS Final Project: Overlay
## Team G
+ GABA Iskander
+ MASALYGINA Kseniia

# How to run the program
Compile the Java classes and binary files.
~~~~
cd src
make
~~~~

Start a chat clients like this:
~~~~
cd ../class
java -cp .:../lib/* Client
~~~~

Or start chat GUI clients like this:
~~~~
cd ../class
java -cp .:../lib/* ClientGUI
~~~~

Or even a mixture of both! They will still be able to communicate fine.

# Docker
For convenience, we used the latest RabbitMQ broker Docker version. It will spin up for you automatically from the `Makefile`

# Clean
Stop the RabbitMQ broker Docker container, remove it, and remove compiled `.class` files.
~~~~
cd ../src
make clean
~~~~

# Implemented Features
+ Chat application
+ Join the chat room.
+ Leave the chat room.
+ Request history.
+ Send private messages to specific clients (terminal version).
+ Broadcast messages to chat room.
+ Keep a list of connected users updated and prevent duplicate usernames.

## Bonus
+ Usage of Docker version of RabbitMQ.
+ Graphical User Interface.

# System Requirements
+ OpenJDK v11 or higher.
+ Docker v19 or higher up and running.

# Comparison with the previous solution
RabbitMQ is more robust, flexible, versatile. It is easier to work with. It has multiple client libraries.
With RabbitMQ we used a new architecture without server. That's why chat history is now saved on the client's machine.
Messages are now saved after every public message is sent. We don't have to worry about a central coordinating node being down anymore. 
The number of lines of code is less than in RMI. 
