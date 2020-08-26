package fr.sorbonne_u.datacenter.software.requestdispatcher.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;


/**
 * The class <code>RequestDispatcherManagementOutboundPort</code> implements the
 * inbound port requiring the interface <code>RequestDispatcherManagementI</code>.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 */
public class RequestDispatcherManagementOutboundPort 
extends		AbstractOutboundPort
implements	RequestDispatcherManagementI{

	
	private static final long serialVersionUID = 1L;
	
	public RequestDispatcherManagementOutboundPort(ComponentI owner) throws Exception {
		
		super(RequestDispatcherManagementI.class, owner) ;
	}

	
	/* Pour le RMI */
	public RequestDispatcherManagementOutboundPort(String uri, ComponentI owner) throws Exception {
			
		super(uri, RequestDispatcherManagementI.class, owner);
	}

	/**
	 * @see fr.sorbonne_u.datacenter.requestdispatcher.interfaces.RequestDispatcherManagementI#disconnectVM()
	 */
	@Override
	public void disconnectVM() throws Exception {
		
		((RequestDispatcherManagementI)this.connector).disconnectVM() ;
		
	}

}
