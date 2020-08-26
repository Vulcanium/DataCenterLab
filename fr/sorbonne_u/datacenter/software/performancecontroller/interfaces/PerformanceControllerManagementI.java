package fr.sorbonne_u.datacenter.software.performancecontroller.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedI;
import fr.sorbonne_u.components.interfaces.RequiredI;

/**
 * L'interface <code>PerformanceControllerManagementI</code> definit 
 * les actions de gestion fournies par le composant du Controleur de Performance.
 */
public interface PerformanceControllerManagementI extends OfferedI, RequiredI{

	/**
	 * Notifications des AVMs lorsqu'elles terminent le traitement de leurs requetes
	 * 
	 * @param VmURis	URI des AVM dont on souhaite la notification
	 * @throws Exception
	 */
	public void notifyEndRequest(String[] VmURis) throws Exception;
	
	
}
