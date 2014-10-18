package pubsub.interfaces;
//******************************************************************************
//File:    Publisher.java
//Package: pubsub.interfaces;
//Unit:    Distributed Programming Individual Project
//******************************************************************************
import pubsub.Event;
import pubsub.Topic;

/**
 * This interface is the methods available to a Publisher within the pub-sub system
 * 
 * @author rob mccartney
 *
 */
public interface Publisher {
	
	/**
	 * Publish an event of a specific topic with title, content, and optional keywords for content filtering
	 * 
	 * @param event to be published
	 */
	public void publish(Event event);

	/**
	 * Advertise new topic for others to subscribe to
	 * 
	 * @param newTopic
	 */
	public void advertise(Topic newTopic);

}
