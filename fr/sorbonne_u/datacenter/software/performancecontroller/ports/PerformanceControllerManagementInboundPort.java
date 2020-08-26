package fr.sorbonne_u.datacenter.software.performancecontroller.ports;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.datacenter.software.performancecontroller.interfaces.PerformanceControllerManagementI;


/**
 * La classe <code>PerformanceControllerManagementInboundPort</code> implemente le
 * port entrant a travers lequel les methodes de gestion des composants sont appelees.
 */
public class PerformanceControllerManagementInboundPort
extends AbstractInboundPort
implements PerformanceControllerManagementI

{

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param owner
	 * @throws Exception
	 */
	public PerformanceControllerManagementInboundPort(ComponentI owner) throws Exception {
		super(PerformanceControllerManagementI.class, owner);
	}

	/**
	 * 
	 * @param uri
	 * @param owner
	 * @throws Exception
	 */
	public PerformanceControllerManagementInboundPort( String uri , ComponentI owner ) throws Exception {
		super( uri, PerformanceControllerManagementI.class, owner );
	}

	/**
	 * @see fr.sorbonne_u.datacenter.performancecontroller.interfaces.PerformanceControllerManagementI#notifyEndRequest(String)
	 */
	@Override
	public void notifyEndRequest(String[] VmURis) throws Exception {

		this.getOwner().handleRequestSync( new AbstractComponent.AbstractService<Void>() {
			@Override
			public Void call() throws Exception {
				((PerformanceControllerManagementI)this.getOwner()).
				notifyEndRequest(VmURis) ;
				return null;
			}
		}) ;
						
	}
	
	
	
	
}
