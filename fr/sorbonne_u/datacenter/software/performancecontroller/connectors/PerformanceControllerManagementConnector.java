package fr.sorbonne_u.datacenter.software.performancecontroller.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.datacenter.software.performancecontroller.interfaces.PerformanceControllerManagementI;

/** 
 * La classe <code>PerformanceControllerManagementConnector</code> implemente un connecteur 
 * pour l'interface de gestion du Controleur de Performance.
 */
public class PerformanceControllerManagementConnector 
extends AbstractConnector
implements PerformanceControllerManagementI {

	/**
	 * @see fr.sorbonne_u.datacenter.performancecontroller.interfaces.PerformanceControllerManagementI#notifyEndRequest(String)
	 */
	@Override
	public void notifyEndRequest(String[] VmURis) throws Exception {
		( ( PerformanceControllerManagementI ) this.offering ).notifyEndRequest( VmURis );	
	}
}
