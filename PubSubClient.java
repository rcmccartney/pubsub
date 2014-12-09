package pubsub;

//******************************************************************************
//File:    PubSubClient.java
//Package: None
//Unit:    Distributed Programming Individual Project
//******************************************************************************

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;

import pubsub.PubSubAgent;
import pubsub.interfaces.EventManInterface;
/**
 * This class parses command-line input in order to set up the client in the client-server architecture.
 * Can be used to changed the hostname and port from default, as well as load a previously saved agent
 * Uses the RMI registry to get the Server object 
 * 
 * @author rob mccartney
 *
 */
public class PubSubClient {

	private String hostName = ""; 
    private int port = 1099;
    private PubSubAgent agent = null;
    
    /**
     * Constructor that makes a new agent or loads a previously saved one
     * 
     * @param args String[] from the command line
     */
    public PubSubClient(String[] args) {

    	if (args.length > 0)  
    		parseArgs(args);
    	try {
    		if (hostName.length() == 0) 
    			hostName = InetAddress.getLocalHost().getHostAddress();
    		if (agent == null) {
    			EventManInterface server = (EventManInterface) Naming.lookup("//" + hostName + ":" + port + "/EventManager");
    			System.out.println("Connected to server at " + hostName + ":" + port );
    			agent = new PubSubAgent(server);
    		}
		} catch (Exception e) {
			System.out.println("Cannot connect to the Event Manager server at this time.  Please try again later.");
			System.out.println("Did you specify the correct hostname and port of the server?");
			System.out.println("Please try again later.");
			System.exit(1);
		}
    }
    
    /**
     * Constructor that is passed an agent that was created outside the class.  Uses setServer method to 
     * set up the relationship
     * 
     * @param args command-line arguments for port and hostname
     * @param agent that will be communicating with the Event server
     */
    public PubSubClient(String[] args, PubSubAgent agent) {

    	if (args.length > 0)  
    		parseArgs(args);
    	try {
    		if (hostName.length() == 0) 
    			hostName = InetAddress.getLocalHost().getHostAddress();
    		EventManInterface server = (EventManInterface) Naming.lookup("//" + hostName + ":" + port + "/EventManager");
    		System.out.println("Connected to server at " + hostName + ":" + port );
    		agent.setServer(server);
		} catch (Exception e) {
			System.out.println("Cannot connect to the Event Manager server at this time.  Please try again later.");
			System.out.println("Did you specify the correct hostname and port of the server?");
			System.out.println("Please try again later.");
			System.exit(1);
		}
    }
	
	/**
	 * This method parses any inputs for the port to use, and stores it into
	 * the instance variable prior to the constructor
	 * 
	 * @param args passed in on command line
	 */
	private void parseArgs(String args[]) {
		
		for (int i = 0; i < args.length; i ++) {	
			if (args[i].equals("-p")) 
				port = new Integer(args[++i]).intValue();
			else if (args[i].equals("-host")) 
				hostName = args[++i];
			else if (args[i].equals("-l")) {
				try {
					ObjectInputStream is = new ObjectInputStream(new FileInputStream("agent.dat"));
				    agent = (PubSubAgent) is.readObject();
				    agent.rebindToServer();
				    is.close();
				} catch (Exception e) {
					System.out.println("Object not loaded correctly.");
					System.out.println("New agent will be created.");
				}
			}
			else {
				System.out.println("Correct usage: java EventServer [-l] [-host <hostName>] [-p <portnumber>]");
				System.out.println("\t-l: loads previously saved pub-sub agent.");
				System.out.println("\t-host: override localhost to set the host to <hostName>.");
				System.out.println("\t-p: override default RMI Registry port 1099 to <port>.  "
						+ "\n\t<port> must match both the 'java EventServer [-p port]' and 'rmiregistry [port]' commands.");
				System.exit(1);
			}
		}
	}
	
	/**
	 * @param args for hostname or port to not be default
	 * @throws RemoteException 
	 */
	public static void main(String[] args) throws RemoteException {
		PubSubClient client = new PubSubClient(args);
		client.agent.commandLineInterface();
	}
}
