package pubsub;
//******************************************************************************
//File:    EventServer.java
//Package: None
//Unit:    Distributed Programming Individual Project
//******************************************************************************
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RemoteException;

/**
 * This class parses command-line input in order to set up the server in the client-server architecture.
 * Can be used to changed the hostname and port from default. Uses the RMI registry to register the Server object 
 * for clients to grab
 *  
 * @author rob mccartney
 *
 */
public class EventServer {

	private int port = 1099;
	private String hostName = "";
	private EventManager manager = null;
	
	/**
	 * Constructor that makes a new EventManager and loads pre-built Topics
	 * @param args command-line arguments for hostname and port
	 */
	public EventServer(String[] args) {
		if (args.length > 0)  
    		parseArgs(args);
    	try {
    		if (hostName.length() == 0) 
    			hostName = InetAddress.getLocalHost().getHostAddress();
    		manager = new EventManager(true);
    		Naming.rebind("//" + hostName + ":" + port + "/EventManager", manager);
            System.out.println("EventManager bound in registry at " + hostName + ":" + port);
            manager.startService();
		} catch (Exception e) {
			System.out.println( "EventManager error");
			System.out.println( "Did you run 'rmiregistry [port] &' first then 'java EventServer [-p <port>]'?" );
			System.exit(1);
		}
	}
	
    /**
     * Constructor that is passed an EventManager that was created outside the class.  
     * 
     * @param args command-line arguments for port and hostname
     * @param manager that will be communicating with the clients
     */
	public EventServer(String[] args, EventManager manager) {
		if (args.length > 0)  
    		parseArgs(args);
    	try {
    		if (hostName.length() == 0) 
    			hostName = InetAddress.getLocalHost().getHostAddress();
    		Naming.rebind("//" + hostName + ":" + port + "/EventManager", manager);
            System.out.println(manager + " bound in registry at " + hostName + ":" + port);
            manager.startService();
		} catch (Exception e) {
			System.out.println( "Binding error");
			System.out.println( "Did you run 'rmiregistry [port] &' before running 'java "+manager+" [-p <port>]'?" );
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
			if (args[i].equals("-p")) port = new Integer(args[++i]).intValue();
			else if (args[i].equals("-host")) hostName = args[++i];
			else {
				System.out.println("Correct usage: java EventServer [-host <hostName>] [-p <portnumber>]");
				System.out.println("  -host: override localhost to set the host to <hostName>.");
				System.out.println("  -p: override default RMI Registry port 1099 to <port>.");
				System.exit(1);
			}
		}
	}

	/**
	 * @param args port number and hostname to use
	 * @throws RemoteException 
	 */
	public static void main(String[] args) throws RemoteException {
		EventServer server = new EventServer(args);
		server.manager.commandLineInterface();
	}
}
