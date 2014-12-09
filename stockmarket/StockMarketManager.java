package pubsub.stockmarket;

//******************************************************************************
//File:    StockMarketManager.java
//Package: None
//Unit:    Distributed Programming Individual Project
//******************************************************************************
import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.Scanner;

import pubsub.Event;
import pubsub.EventManager;
import pubsub.EventServer;
import pubsub.Topic;

/**
 * This class represents the EventManager in a stock-market scenario
 * 
 * @author rob mccartney
 *
 */
public class StockMarketManager extends EventManager {

	private static final long serialVersionUID = 1L;
	public static Topic marketBuy = new Topic("Stock Market Buys", "buy");
	public static Topic marketSell = new Topic("Stock Market Sells", "sell");
	public LinkedHashSet<Event> allEvents;
	
	/**
	 * Coonstructor
	 * 
	 * @throws RemoteException
	 */
	public StockMarketManager() throws RemoteException {
		super(false);
		allEvents = new LinkedHashSet<>();
		super.addTopic(marketBuy);
		super.addTopic(marketSell);
	}

	/**
	 * overrides superclass to store the event for later printing (parent does not store event)
	 */
	public int publish(Event e) throws RemoteException {
		int returnVal = super.publish(e);
		allEvents.add(e);
		return returnVal;
	}
	
	/**
	 * Use this class as the System Admin
	 */
	public void commandLineInterface() throws RemoteException {
		Scanner in = new Scanner(System.in);
		do {
			System.out.println("System Admin:");
			System.out.println(" 1: Show all events");
			System.out.println(" 2: Show all participants");
			System.out.println(" 3: Quit server");
			System.out.print("> ");
			int choice = -1;
			try {
				choice = in.nextInt(); in.nextLine();
			} catch (Exception e) { in.nextLine(); }
			switch (choice) {
				case 1: 
					for (Event e : allEvents)
						System.out.print( e );
					break;
				case 2: showSubscribers(); break;
				case 3: in.close(); System.exit(0); 
				default: System.out.println("Input not recognized");
			}
		} while (true);
	}
	
	/**
	 * Overwrites toString
	 */
	public String toString() {
		return "StockMarketManager";
	}
	
	/**
	 * main 
	 * 
	 * @param args command-line
	 * @throws RemoteException
	 */
	public static void main(String[] args) throws RemoteException {
		EventManager manager = new StockMarketManager();
		new EventServer(args, manager);
		manager.commandLineInterface();
	}
}
