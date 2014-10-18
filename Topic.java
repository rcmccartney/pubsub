package pubsub;
//******************************************************************************
//File:    Topic.java
//Package: pubsub
//Unit:    Distributed Programming Individual Project
//******************************************************************************
import java.io.Serializable;

/**
 * This class is a Topic created by a Publisher that can be subscribed to by a Subscriber
 * 
 * @author rob mccartney
 *
 */
public class Topic implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private final String name;
	private int id;
	private String[] keywords;

	/**
	 * 
	 * @param _name of this Topic, chosen by user
	 * @param _keywords array of String keywords used for content filtering
	 */
	public Topic(String _name, String... _keywords) {
		this.name = _name;
		this.keywords = _keywords;
	}
	/**
	 * @param n unique ID for Topic determing by the server
	 * @return this
	 */
	public Topic setID(int n) {
		id = n;
		return this;
	}
	/**
	 * 
	 * @return the unique ID for this Topic
	 */
	public int getID() {
		return id;
	}
	/**
	 * 
	 * @return String[] keywords for this Topic
	 */
	public String[] getKeywords() {
		return keywords;
	}
	/**
	 * 
	 * @return the String name of this Topic
	 */
	public String getName() {
		return name;
	}
	/**
	 * Override Object toString
	 */
	public String toString() {
		String topic = "Topic " + this.id + "-" + this.name + "\n" +
					   "\tKeywords=";
		for (int i = 0; i < keywords.length; i++) 
			topic += keywords[i] + ((i==keywords.length-1)?"\n":",");
		return topic;
	}
		
	/**
	 * Overrides Object equals in order to hash this topic correctly
	 */
	public boolean equals(Object obj) {
		return this.name.equals(((Topic)obj).name);
	}
	
	/**
	 * Used by the Topic Container's hash to ensure collisions for equivalent underlying topics
	 */
	public int hashCode() {
		return name.hashCode();
	}
}
