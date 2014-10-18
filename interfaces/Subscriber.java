package pubsub.interfaces;
//******************************************************************************
//File:    Subscriber.java
//Package: pubsub.interfaces
//Unit:    Distributed Programming Individual Project
//******************************************************************************
import java.rmi.RemoteException;

import pubsub.Event;
import pubsub.Topic;

/**
 * These are the methods available for the Subscriber in the pub-sub system.  They extend 
 * Remote so that the server can asynchronously call back the subscriber using the notify() method.
 * If the interface extends Remote every method must throw a RemoteException, even if it is never a
 * actually called in a distributed fashion
 * 
 * @author rob mccartney
 *
 */
public interface Subscriber extends java.rmi.Remote {
	
	/**
	 * Subscribe to a topic
	 * 
	 * @param topic to subscribe to
	 * @throws RemoteException
	 */
	public void subscribe(Topic topic) throws RemoteException;
	
	/**
	 * Subscribe to a topic with matching keywords
	 * 
	 * @param keyword the keyword to subscribe to with content filtering
	 * @throws RemoteException
	 */
	public void subscribe(String keyword) throws RemoteException;
	
	/**
	 * Unsubscribe from a topic 
	 * 
	 * @param topic topic to unsubscribe from
	 * @throws RemoteException
	 */
	public void unsubscribe(Topic topic) throws RemoteException;
	
	/**
	 * Unsubscribe from a keyword 
	 * 
	 * @param keyword to unsubscribe from
	 * @throws RemoteException
	 */
	public void unsubscribe(String keyword) throws RemoteException;
	

	/**
	 * Unsubscribe to all subscribed topics
	 * @throws RemoteException
	 */
	public void unsubscribe() throws RemoteException;
	
	/**
	 * 
	 * @param e the event on which to notify the subscriber
	 * @throws RemoteException
	 */
	public void notify(Event e) throws RemoteException;

}
