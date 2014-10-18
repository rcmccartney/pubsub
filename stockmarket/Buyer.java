package pubsub.stockmarket;
//******************************************************************************
//File:    Buyer.java
//Package: None
//Unit:    Distributed Programming Individual Project
//******************************************************************************
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import pubsub.Event;
import pubsub.PubSubAgent;
import pubsub.PubSubClient;
import pubsub.Topic;
import pubsub.interfaces.BuySell;
import pubsub.interfaces.EventManInterface;
/**
 * This class is a buyer inside the market game
 * 
 * @author rob mccartney
 *
 */
public class Buyer extends PubSubAgent implements BuySell {

	private static final long serialVersionUID = 1L;
	//money you start with
	public static final double STARTING_MONEY = 3000.00;
	public static Random rand = new Random();

	private int[] portfolio;
	private double money = STARTING_MONEY;
	//market-wide offer to buy stock
	public static Topic marketBuy; 
	//market-wide offer to sell stock
	public static Topic marketSell;
	//the token that is first-come first serve for the seller to get
	public HashMap<Integer, BuyOffer> token;
		
	/**
	 * Constructor of a Buyer that sets up the initial holdings (which is just money)
	 * 
	 * @param _server the StockMarketManager 
	 * @throws RemoteException
	 */
	public Buyer(EventManInterface _server) throws RemoteException {
		super(_server);
		token = new HashMap<>();
		//randomly set up initial portfolio
		portfolio = new int[Seller.MARKET_BASKET];
		for(int i = 0; i < Seller.MARKET_BASKET; i++)
			portfolio[i] = 0;
	}
	/**
	 * No argument constructor used to start the Client-Server architecture 
	 * @throws RemoteException
	 */
	public Buyer() throws RemoteException {
		this(null);
	}

	/**
	 * After no-argument constructor, use this method to set up the buyer with a 
	 * link to the server and also to create topics that allow you to subscribe to every
	 * buy or sell
	 */
	public void setServer(EventManInterface server) throws RemoteException {
		super.setServer(server);
		marketBuy = findTopic("Stock Market Buys");
		marketSell = findTopic("Stock Market Sells");
	}
	
	/**
	 * Overrides Obj toString
	 */
	public String toString() {
		return "Buyer " + super.ID;
	}
	
	/**
	 * Advertises an offer to sell stock to all subscribers
	 * 
	 * @param in Scanner to read arguments
	 * @throws RemoteException 
	 */ 
	public synchronized void buyStock(Scanner in) throws RemoteException {
		System.out.println("Which stock ID do you want to Buy?");
		int stockID = in.nextInt(); in.nextLine();
		System.out.println("At what price?");
		double price = in.nextDouble(); in.nextLine();
		int ID = server.publish(new Event(marketBuy, "Buy Stock " + stockID,  
								"Buyer_" + super.ID + " offers to buy Stock " +stockID+ " at " +price, 
								""+super.ID, ""+stockID, ""+price ));	
		token.put(ID, new BuyOffer(price, stockID));
	}
	/**
	 * Placeholder to implement interface BuySell
	 */
	public synchronized boolean sell(Integer ID) {
		return false;
	}
	/**
	 * Look at current sell offers to see if you want to buy any
	 * @param in Scanner
	 * @throws NumberFormatException
	 * @throws RemoteException
	 */
	private synchronized void sellOffers(Scanner in) throws NumberFormatException, RemoteException {
		for(Event e : recvdEvents ) 
			System.out.print(e);
		System.out.println("Which offer do you want? Use Event's UniqueID in title to specify");
		int eventID = in.nextInt(); in.nextLine();
		for(Event e : recvdEvents ) {
			if (e.getID() == eventID) {
				int sellID = Integer.parseInt(e.getKeywords()[0]);
				int stockID = Integer.parseInt(e.getKeywords()[1]);
				Double price = Double.parseDouble(e.getKeywords()[2]);
				BuySell seller = (BuySell) server.getSubscriber(sellID);
				if (seller.sell(eventID)) {
					this.money -= price;
					this.portfolio[stockID] += 1;
				}
				recvdEvents.remove(e);
				return;
			}
		}
		System.out.println("Unique ID not recognized");
	}
	
	/**
	 * First seller to call buy will get the token
	 */
	public synchronized boolean buy(Integer ID) throws RemoteException {
		if (token.get(ID) != null) {
			BuyOffer b = token.remove(ID);
			this.money -= b.price;
			this.portfolio[b.shareID] += 1;
			return true;
		}
		return false;
	}
	
	/**
	 * Print this seller's portfolio out
	 */
	public void viewPortfolio() {
		System.out.println("Money: " + money);
		for(int i = 0; i < portfolio.length; i++)
			System.out.println("  Stock " + i + ": " + portfolio[i]);
	}
	/**
	 * How you control the buyer from the command line
	 */
	public void commandLineInterface() throws RemoteException {
		Scanner in = new Scanner(System.in);
		do {
			System.out.println("What would you like to do? Enter choice [1-5]:");
			System.out.println("1: Place offer to buy stock");
			System.out.println("2: Respond to a Sell offer");
			System.out.println("3: View portfolio");
			System.out.println("4: Subscribe to offers on a given stock");
			System.out.println("5: Subscribe to all Sell offers");
			System.out.println("6: Quit");
			System.out.print("> ");
			int choice = -1;
			try {
				choice = in.nextInt(); in.nextLine();
			} catch (Exception e) { in.nextLine(); }
			switch (choice) {
				case 1: buyStock(in); break;
				case 2: sellOffers(in); break;
				case 3: viewPortfolio(); break;
				case 4:
					System.out.println("What is the StockID you want to subscribe to?");
					int anID = in.nextInt(); in.nextLine();
					super.subscribe( ""+anID );
					break;
				case 5: super.subscribe( marketSell ); break;
				case 6: in.close(); fullExit(); break;
				default: System.out.println("Input not recognized");
			}
		} while (true);
	}
	
	/**
	 * main program
	 * 
	 * @param args command line arguments for port and hostname info
	 * @throws RemoteException
	 */
	public static void main(String[] args) throws RemoteException {
		Buyer s = new Buyer();
		new PubSubClient(args, s);
		s.commandLineInterface();
	}
}

class BuyOffer {
	public double price;
	public int shareID;
	
	public BuyOffer(double p, int ID) {
		this.shareID = ID;
		this.price = p;
	}
}
