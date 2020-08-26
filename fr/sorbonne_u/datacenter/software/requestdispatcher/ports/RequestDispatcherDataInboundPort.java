package fr.sorbonne_u.datacenter.software.requestdispatcher.ports;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.DataOfferedI.DataI;
import fr.sorbonne_u.datacenter.ports.AbstractControlledDataInboundPort;
import fr.sorbonne_u.datacenter.software.requestdispatcher.RequestDispatcher;

/**
 * La classe <code>RequestDispatcherDataInboundPort</code> implemente le
 * port entrant a travers lequel les methodes de gestion de l'interface <code>DataRequiredI</code> sont appelees.
 */
public class RequestDispatcherDataInboundPort
extends AbstractControlledDataInboundPort {

	
	private static final long serialVersionUID = 1L;

	
	public RequestDispatcherDataInboundPort( ComponentI owner ) throws Exception {
        super( owner );
        assert owner instanceof RequestDispatcher;
    }

    public RequestDispatcherDataInboundPort( String uri , ComponentI owner ) throws Exception {
        super( uri , owner );
        assert owner instanceof RequestDispatcher;
    }
    
    /**
     * @see fr.sorbonne_u.components.interfaces.DataRequiredI#get()
     */
	@Override
	public DataI get() throws Exception {
		
		final RequestDispatcher rd = ( RequestDispatcher ) this.getOwner();
		return rd.handleRequestSync( new AbstractComponent.AbstractService<DataI>() {
			
			@Override
			public DataI call() throws Exception {
				
				
				return rd.getRequestProcessingTimeAvg();
				
			}
		}) ;
     
		
	}
}


