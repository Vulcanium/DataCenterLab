package fr.sorbonne_u.datacenter.software.performancecontroller.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.datacenter.software.performancecontroller.interfaces.PerformanceControllerManagementI;


/**
 * La classe <code>PerformanceControllerManagementOutboundPort</code> implemente le 
 * port sortant via lequel on appelle les methodes de gestion des composants.
 */
public class PerformanceControllerManagementOutboundPort 
extends AbstractOutboundPort
implements PerformanceControllerManagementI

{

	
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param owner
	 * @throws Exception
	 */
	public PerformanceControllerManagementOutboundPort( ComponentI owner ) throws Exception {
		super( PerformanceControllerManagementI.class , owner );
	}

	/**
	 * 
	 * @param uri
	 * @param owner
	 * @throws Exception
	 */
	public PerformanceControllerManagementOutboundPort( String uri , ComponentI owner ) throws Exception {
		super( uri , PerformanceControllerManagementI.class , owner );
	}
	
	
	/**
	 * @see fr.sorbonne_u.datacenter.performancecontroller.interfaces.PerformanceControllerManagementI#notifyEndRequest(String)
	 */
	@Override
	public void notifyEndRequest(String[] VmURis) throws Exception {
		( ( PerformanceControllerManagementI ) this.connector ).notifyEndRequest(VmURis);
	}

}
