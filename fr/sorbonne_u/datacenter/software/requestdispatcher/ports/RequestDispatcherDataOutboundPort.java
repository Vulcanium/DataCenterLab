package fr.sorbonne_u.datacenter.software.requestdispatcher.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.DataRequiredI.DataI;
import fr.sorbonne_u.datacenter.ports.AbstractControlledDataOutboundPort;
import fr.sorbonne_u.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDataI;
import fr.sorbonne_u.datacenter.software.requestdispatcher.interfaces.RequestDispatcherSensorI;

/**
 * La classe <code>RequestDispatcherDataOutboundPort</code> implemente le 
 * port sortant via lequel on appelle les methodes de gestion de l'interface <code>DataRequiredI</code>.
 */
public class RequestDispatcherDataOutboundPort 
extends AbstractControlledDataOutboundPort {

	private static final long serialVersionUID = 1L;
	
	protected String          requestDispatcherURI;
	
	
	public RequestDispatcherDataOutboundPort( ComponentI owner , String requestDispatcherURI )
            throws Exception {
        super( owner );
        this.requestDispatcherURI = requestDispatcherURI;
    }
	
	public RequestDispatcherDataOutboundPort( String uri , ComponentI owner , String requestDispatcherURI )
			throws Exception {
		super( uri , owner );
		this.requestDispatcherURI = requestDispatcherURI;
	}

	
	/**
	 * @see fr.sorbonne_u.components.interfaces.DataRequiredI#receive(DataI)
	 */
	@Override
	public void receive(DataI d) throws Exception {
		( ( RequestDispatcherSensorI ) this.getOwner() )
        .acceptRequestDispatcherSensorData( this.requestDispatcherURI , ( RequestDispatcherDataI ) d );	
	}

}
