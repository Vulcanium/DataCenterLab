package fr.sorbonne_u.datacenter.software.requestdispatcher.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;

/** 
 * La classe <code>RequestDispatcherManagementConnector</code> implemente un connecteur 
 * pour l'interface de gestion du Request Dispatcher.
 */
public class RequestDispatcherManagementConnector 
extends AbstractConnector
implements RequestDispatcherManagementI {

	
	/**
	 * @see fr.sorbonne_u.datacenter.requestdispatcher.interfaces.RequestDispatcherManagementI#disconnectVM()
	 */
	@Override
	public void disconnectVM() throws Exception {
		( ( RequestDispatcherManagementI ) this.offering ).disconnectVM();
		
	}

	

	

}
