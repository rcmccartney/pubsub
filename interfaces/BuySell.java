package pubsub.interfaces;
//******************************************************************************
//File:    BuySell.java
//Package: pubsub.interfaces;
//Unit:    Distributed Programming Individual Project
//******************************************************************************

import java.rmi.RemoteException;

/**
 * This interface is the methods the StockMarkets clients can call on each other to 
 * conduct the sale process 
 * 
 * @author rob mccartney
 */
public interface BuySell extends java.rmi.Remote {
	
	public boolean buy(Integer ID) throws RemoteException;
	
	public boolean sell(Integer ID) throws RemoteException;
}