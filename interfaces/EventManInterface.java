package pubsub.interfaces;
//******************************************************************************
//File:    EventManInterface.java
//Package: pubsub.interfaces;
//Unit:    Distributed Programming Individual Project
//******************************************************************************
import java.rmi.RemoteException;
import java.util.ArrayList;

import pubsub.Event;
import pubsub.Topic;

/**
 * This interface is the methods the client (both Publisher and Subscriber) can call on the server.
 * Since the interface extends Remote, each method must throw RemoteException
 * 
 * @author rob mccartney
 */
public interface EventManInterface extends java.rmi.Remote {
	
	/**
	 * This method establishes the relationship between server and client
	 * 
	 * @param sub Subscriber registering with the server
	 * @return unique int ID of this client for the Server to track
	 * @throws RemoteException if server is offline
	 */
	public int sayHello(Subscriber sub) throws RemoteException;
	
	/**
	 * This method re-establishes the relationship between server and client
	 * 
	 * @param sub Subscriber re-registering with the server after being away
	 * @param ID Integer that the client received when first registering
	 * @return same ID this client had previously
	 * @throws RemoteException if server is offline
	 */
	public int sayHello(Integer ID, Subscriber sub) throws RemoteException;

	/**
	 * Publishes advertises a new topic
	 * 
	 * @param topic the topic the Publisher wants to advertise
	 * @return unique ID of this Topic
	 * @throws RemoteException if server is offline
	 */
	public int addTopic(Topic topic) throws RemoteException;

	/**
	 * User subscribes to given topic
	 * 
	 * @param subID unique subscriber ID of the client 
	 * @param t topic to subscribe to 
	 * @return boolean on whether subscription was successful
	 * @throws RemoteException
	 */
	public boolean addSubscriber(Integer subID, Topic t) throws RemoteException;
	
	/**
	 * User subscribes to given keyword
	 * 
	 * @param subID  unique subscriber ID of the client 
	 * @param keyword the client wants to subscribe to
	 * @return boolean if it was successful
	 * @throws RemoteException
	 */
	public boolean addSubscriber(Integer subID, String keyword) throws RemoteException;
		 
	/**
	 * User unsubscribes from all topics and keywords
	 * 
	 * @param subID unique subscriber ID of the client 
	 * @return  boolean if user was successfully removed from all topics and keywords
	 * @throws RemoteException
	 */
	public boolean removeSubscriber(Integer subID) throws RemoteException;
	
	/**
	 * User unsubscribes from a given topic 
	 * 
	 * @param subID unique subscriber ID of the client 
	 * @param t topic to unsubscribe from
	 * @return  boolean if user was successfully removed from all topics and keywords
	 * @throws RemoteException
	 */
	public boolean removeSubscriber(Integer subID, Topic t) throws RemoteException;
	
	/**
	 * User stops subscribing to a given keyword
	 * 
	 * @param subID unique subscriber ID of the client 
	 * @param keyword
	 * @return
	 * @throws RemoteException
	 */
	public boolean removeSubscriber(Integer subID,  String keyword) throws RemoteException;

	/**
	 * Publisher publishes an event to those clients that are subscribed to given topic or keywords
	 * 
	 * @param event to be published
	 * @return the unique ID of this event
	 * @throws RemoteException
	 */
	public int publish(Event event) throws RemoteException;
	
	/**
	 * The server will return a list of all available topics for a subscriber to choose from
	 * 
	 * @return ArrayList of Topics on server 
	 * @throws RemoteException
	 */
	public ArrayList<Topic> getTopics() throws RemoteException;
	
	/**
	 * Subscriber sets ID to null while he is offline, saysHello once he comes back online to re-establish
	 * relationship
	 * 
	 * @param ID unique subscriberID that is set to null
	 * @throws RemoteException
	 */
	public void unbind(Integer ID) throws RemoteException;
	
	/**
	 * This user is quitting and will not be returning
	 * 
	 * @param ID unique subscriberID that is removed altogether
	 * @throws RemoteException
	 */
	public void unbindPermanent(Integer ID) throws RemoteException;

	/**
	 * Find a Subscriber by his unique ID
	 * 
	 * @param ID of Subscriber to return
	 * @return Subscriber object
	 * @throws RemoteException
	 */
	public Subscriber getSubscriber(Integer ID) throws RemoteException;
}
