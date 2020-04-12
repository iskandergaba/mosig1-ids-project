# IDS Final Project: Overlay
## Team G
+ GABA Iskander
+ MASALYGINA Kseniia

## RMI vs. RabbitMQ
RabbitMQ, duh.

## How to run the program
Compile the Java classes and binary files.
~~~~
cd src
make
~~~~

Start a chat `Main` like this:
~~~~
cd ../class
java -cp .:../lib/* Main topology-connected
~~~~

## Docker
For convenience, we used the latest RabbitMQ broker Docker version. It will spin up for you automatically from the `Makefile`

## Clean
Stop the RabbitMQ broker Docker container, remove it, and remove compiled `.class` files.
~~~~
cd ../src
make clean
~~~~

## Implemented Features
+ Parse an adjacency matrix, check it represents a connected graph, and make RabbitMQ nodes from it.
+ Find the shortest path between each two nodes and create a routing table for each node such that a node knows just the next hop it should make in order to reach a given target node.
+ RabbitMQ nodes can send, route, and receive messages.

## Bonus

# System Requirements
+ OpenJDK v11 or higher.
+ Docker v19 or higher up and running. 
