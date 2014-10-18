package pubsub;
//******************************************************************************
//File:    Event.java
//Package: pubsub
//Unit:    Distributed Programming Individual Project
//******************************************************************************
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * This class represents a single event that can be published or subscribed by the system
 * The Event itself holds the list of all Subscribers that want to receive a copy of it
 * 
 * @author rob mccartney
 *
 */
public class Event implements Serializable {

	private static final long serialVersionUID = 1L;
	private int id = 0;
	private Topic topic;
	private String title;
	private String content;
	private String[] keywords;
	private LinkedHashSet<Integer> toBeNotified;
	
	/**
	 * Constructor
	 *  
	 * @param _t Topic of event
	 * @param _title title of event
	 * @param _content the content
	 * @param _keywords used for content filtering. if none are passed in, this defaults to the Topic keywords
	 */
	public Event(Topic _t, String _title, String _content, String... _keywords) {
		this.topic = _t;
		this.title = _title;
		this.content = _content;
		this.keywords = _keywords;
		if (keywords == null)
			this.keywords = topic.getKeywords();
		toBeNotified = new LinkedHashSet<>();
	}
	
	/**
	 * Used by the server to set the ID once it has been published
	 * @param n the ID to set this too
	 * @return this instance
	 */
	public Event setID(int n) {
		this.id = n;
		return this;
	}
	
	/**
	 * 
	 * @return this unique Event ID
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * 
	 * @return the Topic of this Event
	 */
	public Topic getTopic() {
		return topic;
	}
	
	/**
	 * 
	 * @return the title of this Event
	 */
	public String getTitle() {
		return this.title;
	}
	
	/**
	 * 
	 * @return the content of this Event
	 */
	public String getContent() {
		return content;
	}
	
	/**
	 * 
	 * @return keywords used for filtering that match to this event
	 */
	public String[] getKeywords() {
		return keywords;
	}
	
	/**
	 * Override the Obj equals in order to hash an Event correctly, where an Event is uniquely
	 * determined by its Topic and Title
	 */
	public boolean equals(Object obj) {
		Event e = (Event) obj;
		return this.topic.equals(e.topic) && this.title.equals(e.title);
	}
	
	/**
	 * Must override the regular hashCode in order to hash Events to collide with duplicates
	 */
	public int hashCode() {
		return topic.hashCode() + title.hashCode();
	}
	
	/**
	 * human-readable representation of this Event
	 */
	public String toString() {
		String event = "Event " +this.id +"-"+ this.title + "\n" +
					   "\tPublished under Topic "+ topic.getID() +"-"+ topic.getName() + "\n" +
					   "\tKeywords=";
		for (int i = 0; i < keywords.length; i++) 
			event += keywords[i] + ((i==keywords.length-1)?"\n":",");
		event += "\tContent: " + content + "\n";
		return event;
	}
	
	/**
	 * 
	 * @return iterator to allow for removal of Subscribers as they are notified one by one about this event
	 */
	public synchronized Iterator<Integer> iterator() {
		return toBeNotified.iterator();
	}

	/**
	 * 
	 * @return number of users left to notify
	 */
	public synchronized int notifySize() {
		return toBeNotified.size();
	}
	
	/**
	 * 
	 * @param c some other list of subscribers (either content or topic filtering) to be added to the 
	 * list of subscribers of this event
	 * @return true/false that the list was successfully added
	 */
	public synchronized boolean addSubscriberList(Collection<Integer> c) {
		if (c != null)
			return toBeNotified.addAll(c);
		return false;
	}
}
