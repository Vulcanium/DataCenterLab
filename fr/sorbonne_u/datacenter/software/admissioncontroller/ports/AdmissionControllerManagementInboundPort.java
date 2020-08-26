package fr.sorbonne_u.datacenter.software.admissioncontroller.ports;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.datacenter.software.admissioncontroller.AdmissionController;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.AdmissionControllerHandlerI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;


/**
 * La classe <code>AdmissionControllerManagementInboundPort</code> implemente le
 * port entrant a travers lequel les methodes de gestion des composants sont appelees.
 */
public class AdmissionControllerManagementInboundPort 
extends AbstractInboundPort
implements AdmissionControllerManagementI 

{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	owner instanceof <AdmissionControllerManagementI
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param owner			owner component.
	 * @throws Exception		<i>todo.</i>
	 */
	
	public AdmissionControllerManagementInboundPort(ComponentI owner) throws Exception {
		
			super(AdmissionControllerManagementI.class, owner) ;

			assert	owner instanceof AdmissionControllerManagementI ;
			
	}
	
	/**
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	uri != null and owner instanceof AdmissionControllerManagementI
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param uri			uri of the port.
	 * @param owner			owner component.
	 * @throws Exception		<i>todo.</i>
	 */
	
	/* Variante du constructeur avec URI, pour communiquer en RMI */
	public AdmissionControllerManagementInboundPort(String uri, ComponentI owner) throws Exception {
		
			super(uri, AdmissionControllerManagementI.class, owner);

			assert	uri != null && owner instanceof AdmissionControllerManagementI ;
		}

	
	/**
	 * @see fr.sorbonne_u.datacenter.admissioncontroller.interfaces.AdmissionControllerManagementI#demandeHebergement(String, int)
	 */
	@Override
	public String demandeHebergement(String requestNotificationInboundPortURI, int nbCores) throws Exception {
		
		return this.getOwner().handleRequestSync(
				new AbstractComponent.AbstractService<String>() {
					
					@Override
					public String call() throws Exception {
						return ((AdmissionControllerHandlerI)this.getOwner()).allouerVM(requestNotificationInboundPortURI, nbCores);
					}
					
				});
		
	}
	

	/**
	 * @see fr.sorbonne_u.datacenter.admissioncontroller.interfaces.AdmissionControllerManagementI#connexionHote(String)
	 */
	@Override
	public Boolean connexionHote(String requestNotificationInboundPortURI) throws Exception {
		
		return this.getOwner().handleRequestSync(
				new AbstractComponent.AbstractService<Boolean>() {
					
					@Override
					public Boolean call() throws Exception {
						return ((AdmissionControllerHandlerI)this.getOwner()).handlerConnexionHote(requestNotificationInboundPortURI);
					}
					
				});
		
	}

	
	/**
	 * @see fr.sorbonne_u.datacenter.admissioncontroller.interfaces.AdmissionControllerManagementI#allouerVM(String, int)
	 */
	@Override
	public String allouerVM(String requestNotificationInboundPortURI, int nbCores) throws Exception {
		final AdmissionController ac = ( AdmissionController ) this.getOwner();

        return ac.handleRequestSync( new AbstractComponent.AbstractService<String>() {

            @Override
            public String call() throws Exception {
                return ac.allouerVM(requestNotificationInboundPortURI, nbCores);
            }
        } );
	}


}
