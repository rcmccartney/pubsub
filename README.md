pubsub
======

Publisher-Subscriber system using RMI and asynchronous communication for message passing in a distributed system.

This is an implementation of a distributed publisher-subscriber system
using Java Remote Method Invocation (RMI). It uses multi-threading with
thread-per-request and does not block for send or receive.  Both the
server and the client have a command line interface for publishing
events, advertising events, subscribing to topics or keywords, and
viewing past events received.  The system supports both Topic-based and
Content-based filtering of events, where the content filtering is based
off of keyword arguments.  They keywords can be defined by the Topic or
they can be defined by the Event, and if this keyword matches the
subscribed keyword of a client then the client will receive the message.
Using the PubSub system, there is also a small implementation of a stock
market, where buyers and sellers communicate to one another through
Pub-Sub and then use RMI to finalize buys or sells.

To run the PubSub program first extract source files then open a shell and type:

$ rmiregistry [port] &    //port is optional
$ java EventServer [-host <hostName>] [-p <portnumber>]  //optional command-line arguments

The port number must match what you used for the rmiregistry.  Then, to connect with a PubSubAgent, run

$ java PubSubClient [-l] [-host <hostName>] [-p <portnumber>]

The hostName must match what the server is running on.  If you are using two terminals on the same machine,
this argument is not needed.  The portnumber must match what the server and rmiregistry used.
Use -l to load a previously saved Client to show asynchronous capabilities.  Upon being loaded, 
Client will receive any messages published while he was offline.

Example run:
(On buddy.cs.rit.edu)
$ rmiregistry &
$ java EventServer

(On a different machine)
$ java PubSubClient -host buddy.cs.rit.edu
(Or on a different shell)
$ java PubSubClient 


To run the Stock Exchange the commands are similar but the main classes have changed.
Follow the following example:

Example run:
(On buddy.cs.rit.edu)
$ rmiregistry &
$ java StockMarketManager

(On a different machine)
$ java Seller -host buddy.cs.rit.edu
$ java Buyer -host buddy.cs.rit.edu
