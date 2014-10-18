package pubsub;
//******************************************************************************
//File:    EventManager.java
//Package: pubsub
//Unit:    Distributed Programming Individual Project
//******************************************************************************
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Scanner;

import pubsub.interfaces.EventManInterface;
import pubsub.interfaces.Subscriber;
/**
 * This class is the server that all publishers and subscribers work through for asynchronous message passing.
 * 
 * @author rob mccartney
 *
 */
public class EventManager extends UnicastRemoteObject implements EventManInterface {
	
	private static final long serialVersionUID = 1L;
	//Amount of time to wait between attempts to contact a non-responsive agent
	public static final int TIMEOUT = 1000;
	//counters used to assign Unique IDs
	protected Integer topicID = 0;
	protected Integer subscriberID = 0;
	protected Integer eventID = 0;
	//Storage for all Topic Containers (topic plus subscribers)
	protected LinkedHashSet<TopicContainer> allTopicContainers;
	//Events are stored here while they continue to try to contact a missing subscriber
	protected LinkedList<Event> pendingEvents;
	// Maps from the name of a keyword to the ID of the clients that receive those keyword events
	// in order to allow for efficient content-filtering
	protected HashMap<String, LinkedHashSet<Integer>> contentFilter;
	// Maps from the ID of a client to the actual RMI object of the client 
	// This allows the client to leave and come back later without 
	//changing the unique identifier
	protected HashMap<Integer, Subscriber> clientBinding;

	/**
	 * Constructor
	 * @param preload whether to load a pre-made selection of topics at startup
	 * @throws RemoteException for RMI errors
	 */
	public EventManager(boolean preload) throws RemoteException {
		allTopicContainers = new LinkedHashSet<>();
		pendingEvents = new LinkedList<>();
		contentFilter = new HashMap<>();
		clientBinding = new HashMap<>();
		if (preload)
			this.loadPrebuiltTopics();
	}
	
	/**
	 * Gives pre-made topics to start with rather than creating them all.
	 * The topics are stored in topics.dat as the following format:
	 * topic name:keyword keyword keyword...
	 * where the topic name is to the left of the colon and keywords follow, 
	 * separated from each other by whitespace 
	 */
	private void loadPrebuiltTopics() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(new File("topics.dat")));
			String line;
			while( (line =in.readLine()) != null) {
				String[] topicData = line.split(":");
				this.addTopic(new Topic(topicData[0], topicData[1].split("\\s+")) );
			}
			in.close();
		} catch (Exception e) { System.out.println("Error loading prebuilt topics."); }
	}

	/**
	 * see interface javadoc
	 */
	public int sayHello(Subscriber sub) throws RemoteException {
		synchronized (clientBinding) {
			clientBinding.put(++subscriberID, sub);
			return subscriberID;
		}
	}
	
	/**
	 * see interface javadoc
	 */
	public int sayHello(Integer ID, Subscriber sub) throws RemoteException {
		synchronized (clientBinding) {
			clientBinding.put(ID, sub);
			return ID;
		}
	}
	
	/**
	 * see interface javadoc
	 */
	public void unbind(Integer ID) {
		synchronized (clientBinding) {
			clientBinding.put(ID, null);
		}
	}
	/**
	 * see interface javadoc
	 */
	public void unbindPermanent(Integer ID) {
		synchronized (clientBinding) {
			clientBinding.remove(ID);
		}
	}
	
	public Subscriber getSubscriber(Integer ID) {
		return clientBinding.get(ID);
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	//  Asynchronous notification service
	////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * This is the method that runs in the background to contact all the Subscribers continually when they 
	 * are offline until they return
	 */
	public void startService() {
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				while(true) {
					try { Thread.sleep(TIMEOUT); } catch (InterruptedException e1) { }
					synchronized(pendingEvents) {
						while (pendingEvents.isEmpty()) {
							try {  
								pendingEvents.wait(); 
							} catch (Exception e) { }
						}
						asynchNotify();
					}
				}
			}
			/**
			 * helper method that iterates through each event that is pending compelte 
			 * notification of its subscribers
			 */
			public void asynchNotify() {
				Iterator<Event> event_iter = pendingEvents.iterator();
				while( event_iter.hasNext() ) {
					if (notifySubscribers(event_iter.next()) == 0) 
						event_iter.remove();	
				}
			}
		});
		//Daemon allows this thread not to block program from exiting
		t.setDaemon(true);
		t.start();		
	}

	
	////////////////////////////////////////////////////////////////////////////////////
	//  Publisher services 
	////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Helper method to notify all subscribers of a given event.  Subscribers have been added based on both 
	 * Topic filtering and content filtering at this point.  As subscribers are contacted, they are removed from
	 * the internal list of the event.  When the event subscriber list is empty, then the event is removed from
	 * the lsit of all pending events
	 * 
	 * @param event Event to notify subscribers of
	 */
	public int notifySubscribers(Event event) {
		Iterator<Integer> sub_iter = event.iterator();
		while (sub_iter.hasNext()) {
			try {
				Integer subID = sub_iter.next();
				if (clientBinding.get(subID) != null) {
					clientBinding.get(subID).notify(event);
					sub_iter.remove();
				}
			} catch(RemoteException e) { } //Do nothing on remote exception, try again later 
		}
		//when this returns 0, we know every subscriber has received the message
		return event.notifySize();
	}
	
	/**
	 * see interface javadoc
	 */
	public int publish(Event event) throws RemoteException {
		if (event.getID() != 0) {
			System.err.println("Event has already been published.");
			return 0;
		}
		synchronized (allTopicContainers) {
			for( TopicContainer tc : allTopicContainers) {
				if (tc.getTopic().getID() == event.getTopic().getID() ) {
					event.setID(++eventID).addSubscriberList(tc.getSubscribers());
					for(String key : event.getKeywords() )
						event.addSubscriberList( contentFilter.get(key) );
					if (notifySubscribers(event) > 0) {
						synchronized (pendingEvents) {
							pendingEvents.add(event);
							pendingEvents.notifyAll();
						}
					}
					return eventID;
				}
			}
		}
		System.err.println("Event topic not found.");
		return 0;
	}
	
	/**
	 * see interface javadoc
	 */
	public int addTopic(Topic topic) throws RemoteException {
		synchronized (allTopicContainers) {
			if (allTopicContainers.add( new TopicContainer(topic) )) {
				topic.setID(++topicID);
				return topicID;
			}
			return 0;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	//  Subscriber services 
	////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * see interface javadoc
	 */
	public boolean addSubscriber(Integer subID, Topic topic) throws RemoteException {
		for( TopicContainer tc : allTopicContainers) {
			if (tc.getTopic().getID() == topic.getID() ) {
				return tc.addSubscriber(subID);
			}
		}
		return false;
	}
	
	/**
	 * see interface javadoc
	 */
	public boolean addSubscriber(Integer subID, String keyword) throws RemoteException {
		if (contentFilter.get(keyword) != null)
			return contentFilter.get(keyword).add(subID);
		else {
			contentFilter.put(keyword, new LinkedHashSet<Integer>());
			return contentFilter.get(keyword).add(subID);
		}
	}

	/**
	 * see interface javadoc
	 */
	public boolean removeSubscriber(Integer subID) throws RemoteException {
		for( TopicContainer tc : allTopicContainers)
			tc.removeSubscriber(subID);
		for(String key : contentFilter.keySet()) {
			removeSubscriber(subID, key);
		}
		return true;
	}
	
	/**
	 * see interface javadoc
	 */
	public boolean removeSubscriber(Integer subID, String keyword) throws RemoteException {
		Iterator<Integer> it = contentFilter.get(keyword).iterator();
		while (it.hasNext()) {
			if (it.next().equals(subID)) {
				it.remove();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * see interface javadoc
	 */
	public boolean removeSubscriber(Integer subID, Topic topic) throws RemoteException {
		for( TopicContainer tc : allTopicContainers) {
			if (tc.getTopic().getID() == topic.getID() ) {
				return tc.removeSubscriber(subID);
			}
		}
		return false;
	}
	
	/**
	 * see interface javadoc
	 */
	public ArrayList<Topic> getTopics() {
		synchronized (allTopicContainers) {
			ArrayList<Topic> topics = new ArrayList<>();
			for (TopicContainer tc : allTopicContainers)
				topics.add( tc.getTopic() );
			return topics;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	//  Command-line interface services 
	////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This is how a user interacts with the server side of the pub-sub system.  Allows for
	 * 1. Showing all topics
	 * 2. Showing all subscribers
	 * 3. Quitting
	 * Note you cannot show all events because they are not stored after being fully delivered
	 * 
	 * @throws RemoteException
	 */
	public void commandLineInterface() throws RemoteException {
		Scanner in = new Scanner(System.in);
		do {
			System.out.println("What would you like to do? Enter choice [1-3]:");
			System.out.println(" 1: Show topics");
			System.out.println(" 2: Show subscribers");
			System.out.println(" 3: Quit server");
			System.out.print("> ");
			int choice = -1;
			try {
				choice = in.nextInt(); in.nextLine();
			} catch (Exception e) { in.nextLine(); }
			switch (choice) {
				case 1: 
					for (TopicContainer tc : allTopicContainers)
						System.out.print( tc.getTopic() );
					break;
				case 2: showSubscribers(); break;
				case 3: in.close(); System.exit(0); 
				default: System.out.println("Input not recognized");
			}
		} while (true);
	}
	
	/**
	 * show the complete list of subscribers, used by server for command line printing
	 * Prints both all the subscribers to each topic and all the subscribers to each 
	 * keyword
	 */
	public void showSubscribers() throws RemoteException {
		for( TopicContainer tc : allTopicContainers) 
			System.out.print("Topic: " +tc.getTopic().getName()+ "\n" +
							 "\tSubscribers: " + tc.printSubscribers());
		String contentPrint = "";
		for( String key : contentFilter.keySet() ) {
			contentPrint += "Keyword: " + key + "\n\tSubscribers: ";
			int i = contentFilter.get(key).size();
			if (i == 0)
				contentPrint += "None\n";
			for ( Integer subID : contentFilter.get(key) )
				contentPrint += "Agent_" + subID + ((--i > 0)?",":"\n");
		}
		System.out.print(contentPrint);
	}
}
