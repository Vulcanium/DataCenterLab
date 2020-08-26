package fr.sorbonne_u.datacenter.software.admissioncontroller.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;

/**
 * La classe <code>AdmissionControllerManagementOutboundPort</code> implemente le 
 * port sortant via lequel on appelle les methodes de gestion des composants.
 */
public class AdmissionControllerManagementOutboundPort 
extends		AbstractOutboundPort
implements	AdmissionControllerManagementI {

	
	private static final long serialVersionUID = 1L;

	public AdmissionControllerManagementOutboundPort(ComponentI owner) throws Exception {
		
		super(AdmissionControllerManagementI.class, owner);
	}
	
	
	public AdmissionControllerManagementOutboundPort(String uri, ComponentI owner) throws Exception {
		
		super(uri, AdmissionControllerManagementI.class, owner);
	}
	

	/**
	 * @see fr.sorbonne_u.datacenter.admissioncontroller.interfaces.AdmissionControllerManagementI#demandeHebergement(String, int)
	 */
	@Override
	public String demandeHebergement(String requestNotificationInboundPortURI, int nbCores) throws Exception {
		return ((AdmissionControllerManagementI)this.connector).demandeHebergement(requestNotificationInboundPortURI, nbCores);
	}

	
	/**
	 * @see fr.sorbonne_u.datacenter.admissioncontroller.interfaces.AdmissionControllerManagementI#connexionHote(String)
	 */
	@Override
	public Boolean connexionHote(String requestNotificationInboundPortURI) throws Exception {
		return ((AdmissionControllerManagementI)this.connector).connexionHote(requestNotificationInboundPortURI);
	}


	/**
	 * @see fr.sorbonne_u.datacenter.admissioncontroller.interfaces.AdmissionControllerManagementI#allouerVM(String, int)
	 */
	@Override
	public String allouerVM(String requestNotificationInboundPortURI, int nbCores) throws Exception {
		return ( ( AdmissionControllerManagementI ) this.connector ).allouerVM(requestNotificationInboundPortURI, nbCores);
	}
	
	
}
