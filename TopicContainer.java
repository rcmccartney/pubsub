package pubsub;
//******************************************************************************
//File:    TopicContainer.java
//Package: pubsub
//Unit:    Distributed Programming Individual Project
//******************************************************************************
import java.util.Iterator;
import java.util.LinkedHashSet;
/**
 * 
 * This is a class for the server to hold a Topic in along with the subscribers to that topic.
 * This way, the server can give the individual Topics to other clients that ask to see what is 
 * available without giving away who is subscribed to each Topic. The Container knows how to add,
 * remove, and iterate over its subscribers, to the server uses these methods to manage Topic-based
 * subscriptions.
 * 
 * @author rob mccartney
 *
 */
public class TopicContainer {

	//two parts to a container, the topic and the subscribers
	private Topic topic;
	//the Integer is the Unique ID of the client subscriber
	//Use a LinkedHashSet to allow O(1) lookup and efficient
	//iteration
	private LinkedHashSet<Integer> topicSubscribers;
	
	/**
	 * Constructor
	 * @param _topic that will be held in this container
	 */
	public TopicContainer(Topic _topic) {
		this.topic = _topic;
		topicSubscribers = new LinkedHashSet<>();
	}
	/**
	 * 
	 * @return the underlying topic
	 */
	public Topic getTopic() {
		return topic;
	}
	
	/**
	 * Synchronously add a new subscriber to the set.  Needs to be synchronous to prevent a 
	 * concurrent modification during iterating
	 * 
	 * @param subID the subscriber ID to add to this container
	 * @return boolean on success or failure. Fails when the user is already subscribed
	 */
	public synchronized boolean addSubscriber(Integer subID) {
		return topicSubscribers.add(subID);
	}
	
	/**
	 * Synchronously removed a subscriber from the set.  Needs to be synchronous to prevent a 
	 * concurrent modification during iterating
	 * 
	 * @param subID the subscriber ID to remove from this container
	 * @return boolean on success or failure. Fails when the user being removed is not subscribed
	 */
	public synchronized boolean removeSubscriber(Integer subID) {
		return topicSubscribers.remove(subID);
	}
	
	/**
	 * Synchronously get the size of the underlying data structure
	 * 
	 * @return int size of the underlying hashset of subscribers
	 */
	public synchronized int getSubscriberSize() {
		return topicSubscribers.size();
	}
	
	/**
	 * Synchronously return the underlying hashset so that it can be added to another list (inside event)
	 * 
	 * @return LinkedHashSet<Integer> the ID's of the subscribers
	 */
	public synchronized LinkedHashSet<Integer> getSubscribers() {
		return topicSubscribers;
	}
	
	/**
	 * Use the iterator to iterate through the hash and allow removal during iteration
	 * 
	 * @return Iterator<Integer> to iterate through the subscriber ID's
	 */
	public synchronized Iterator<Integer> iterator() {
		return topicSubscribers.iterator();
	}
	
	/**
	 * Overrides the Object equals so that we will not allow two equivalent TopicContainers into a HashSet
	 * as determined by their underlying Topic
	 */
	public boolean equals(Object obj) {
		return topic.equals( ((TopicContainer)obj).getTopic() );
	}
	
	/**
	 * This makes sure equivalent containers actually collide when they are hashed, otherwise we could 
	 * not find duplicates
	 */
	public int hashCode() {
		return topic.hashCode();
	}
	
	/**
	 * Synchronously iterate through the list of subscribers to this topic to print them out
	 * 
	 * @return String of the subscribers currently subscribed to the underlying topic
	 */
	public synchronized String printSubscribers() {
		String formatted = "";
		int i = topicSubscribers.size();
		if (i == 0)
			formatted += "None\n";
		for ( Integer subID : topicSubscribers )
			formatted += "Agent_" + subID + ((--i > 0)?",":"\n");
		return formatted;
	}
	
	/**
	 * Synchronously overrides the Object toString 
	 */
	public synchronized String toString() {
		return topic.toString() + "\n\tSubscribers: " + this.printSubscribers();
	}
}
