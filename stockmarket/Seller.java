package pubsub.stockmarket;
//******************************************************************************
//File:    Seller.java
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
 * This class is a seller inside the market game
 * 
 * @author rob mccartney
 *
 */
public class Seller extends PubSubAgent implements BuySell {

	private static final long serialVersionUID = 1L;
	//total number of stocks in the market
	public static final int MARKET_BASKET = 5;
	//most shares you can own to start
	public static final int MAX_SHARES = 10;
	//money you start with
	public static final double STARTING_MONEY = 1000.00;
	public static Random rand = new Random();

	private int[] portfolio;
	private double money = STARTING_MONEY;
	//market-wide offer to buy stock
	public static Topic marketBuy; 
	//market-wide offer to sell stock
	public static Topic marketSell;
	//the token that is first-come first serve for the buyer to get
	public HashMap<Integer, SaleOffer> token;
		
	/**
	 * Constructor of a Seller that sets up the initial holdings
	 * 
	 * @param _server the StockMarketManager 
	 * @throws RemoteException
	 */
	public Seller(EventManInterface _server) throws RemoteException {
		super(_server);
		token = new HashMap<>();
		//randomly set up initial portfolio
		portfolio = new int[MARKET_BASKET];
		for(int i = 0; i < MARKET_BASKET; i++)
			portfolio[i] = rand.nextInt(MAX_SHARES);
	}
	/**
	 * No argument constructor used to start the Client-Server architecture 
	 * @throws RemoteException
	 */
	public Seller() throws RemoteException {
		this(null);
	}

	/**
	 * After no-argument constructor, use this method to set up the seller with a 
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
		return "Seller " + super.ID;
	}
	
	/**
	 * Advertises an offer to sell stock to all subscribers
	 * 
	 * @param in Scanner to read arguments
	 * @throws RemoteException 
	 */ 
	public synchronized void sellStock(Scanner in) throws RemoteException {
		System.out.println("Which stock ID do you want to sell?");
		int stockID = in.nextInt(); in.nextLine();
		System.out.println("At what price?");
		double price = in.nextDouble(); in.nextLine();
		int ID = server.publish(new Event(marketSell, "Sell Stock " + stockID,  
								"Seller_" + super.ID + " offers to sell Stock " +stockID+ " at " +price, 
								""+super.ID, ""+stockID, ""+price ));	
		token.put(ID, new SaleOffer(price, stockID));
	}
	/**
	 * First buyer to call this method gets the token
	 */
	public synchronized boolean sell(Integer ID) {
		if (token.get(ID) != null) {
			SaleOffer s = token.remove(ID);
			this.money += s.price;
			this.portfolio[s.shareID] -= 1;
			return true;
		}
		return false;
	}
	/**
	 * Look at current buyOffers to see if you want any 
	 * @param in
	 * @throws NumberFormatException
	 * @throws RemoteException
	 */
	private synchronized void buyOffers(Scanner in) throws NumberFormatException, RemoteException {
		for(Event e : recvdEvents ) 
			System.out.print(e);
		System.out.println("Which offer do you want? Use Event's UniqueID in title to specify");
		int eventID = in.nextInt(); in.nextLine();
		for(Event e : recvdEvents ) {
			if (e.getID() == eventID) {
				int buyID = Integer.parseInt(e.getKeywords()[0]);
				int stockID = Integer.parseInt(e.getKeywords()[1]);
				Double price = Double.parseDouble(e.getKeywords()[2]);
				BuySell buyer = (BuySell) server.getSubscriber(buyID);
				if (buyer.buy(eventID)) {
					this.money += price;
					this.portfolio[stockID] -= 1;
				}
				recvdEvents.remove(e);
				return;
			}
		}
		System.out.println("Unique ID not recognized");
	}
	
	/**
	 * Placeholder to implement interface BuySell
	 */
	public synchronized boolean buy(Integer ID) throws RemoteException {
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
	 * How you control the seller from the command line
	 */
	public void commandLineInterface() throws RemoteException {
		Scanner in = new Scanner(System.in);
		do {
			System.out.println("What would you like to do? Enter choice [1-5]:");
			System.out.println("1: Place offer to sell stock");
			System.out.println("2: Respond to a Buy offer");
			System.out.println("3: View portfolio");
			System.out.println("4: Subscribe to offers on a given stock");
			System.out.println("5: Subscribe to all Buy offers");
			System.out.println("6: Quit");
			System.out.print("> ");
			int choice = -1;
			try {
				choice = in.nextInt(); in.nextLine();
			} catch (Exception e) { in.nextLine(); }
			switch (choice) {
				case 1: sellStock(in); break;
				case 2: buyOffers(in); break;
				case 3: viewPortfolio(); break;
				case 4:
					System.out.println("What is the StockID you want to subscribe to?");
					int anID = in.nextInt(); in.nextLine();
					super.subscribe( ""+anID );
					break;
				case 5: super.subscribe( marketBuy ); break;
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
		Seller s = new Seller();
		new PubSubClient(args, s);
		s.commandLineInterface();
	}
}

class SaleOffer {
	public double price;
	public int shareID;
	
	public SaleOffer(double p, int ID) {
		this.shareID = ID;
		this.price = p;
	}
}
