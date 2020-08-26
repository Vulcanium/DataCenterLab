package fr.sorbonne_u.datacenter.software.requestdispatcher.ports;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;


/**
 * The class <code>RequestDispatcherManagementInboundPort</code> implements the
 * inbound port offering the interface <code>RequestDispatcherManagementI</code>.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		owner instanceof RequestDispatcherManagementI
 * </pre>
 */

public class RequestDispatcherManagementInboundPort
extends		AbstractInboundPort
implements	RequestDispatcherManagementI{

	
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	owner instanceof <RequestDispatcherManagementI
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param owner			owner component.
	 * @throws Exception		<i>todo.</i>
	 */
	
	public RequestDispatcherManagementInboundPort(ComponentI owner) throws Exception {
		
			super(RequestDispatcherManagementI.class, owner) ;

			assert	owner instanceof RequestDispatcherManagementI ;
			
	}
	
	/**
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null and owner instanceof RequestManagerManagementI
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri			uri of the port.
	 * @param owner			owner component.
	 * @throws Exception		<i>todo.</i>
	 */
	
	/* Variante du constructeur avec URI, pour communiquer en RMI */
	public RequestDispatcherManagementInboundPort(String uri, ComponentI owner) throws Exception {
		
			super(uri, RequestDispatcherManagementI.class, owner);

			assert	uri != null && owner instanceof RequestDispatcherManagementI ;
		}
	
	
	/**
	 * @see fr.sorbonne_u.datacenter.requestdispatcher.interfaces.RequestDispatcherManagementI#disconnectVM()
	 */
	@Override
	public void disconnectVM() throws Exception {
		
		this.getOwner().handleRequestSync( new AbstractComponent.AbstractService<Void>() {
			
			@Override
			public Void call() throws Exception {
				
				((RequestDispatcherManagementI)this.getOwner()).
				disconnectVM();
				return null;
			}
			
		} );
		
	}

}
