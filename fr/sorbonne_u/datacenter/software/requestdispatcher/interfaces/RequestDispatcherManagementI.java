package fr.sorbonne_u.datacenter.software.requestdispatcher.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedI;
import fr.sorbonne_u.components.interfaces.RequiredI;

/**
 * L'interface <code>RequestDispatcherManagementI</code> definit 
 * les actions de gestion fournies par le composant du RequestDispatcher.
 */
public interface RequestDispatcherManagementI 
extends OfferedI, RequiredI {
	
	/**
	 * Deconnecte la VM du RequestDispatcher.
	 * 
	 * @throws Exception
	 */
	public void disconnectVM() throws Exception;
	
}
