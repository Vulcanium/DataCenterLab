package fr.sorbonne_u.datacenter.software.admissioncontroller.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;

/** 
 * La classe <code>AdmissionControllerManagementConnector</code> implemente un connecteur 
 * pour l'interface de gestion du Controleur d'Admission.
 */
public class AdmissionControllerManagementConnector 
extends AbstractConnector
implements AdmissionControllerManagementI {

	/**
	 * @see fr.sorbonne_u.datacenter.admissioncontroller.interfaces.AdmissionControllerManagementI#demandeHebergement(String, int)
	 */
	@Override
	public String demandeHebergement(String requestNotificationInboundPortURI, int nbCores) throws Exception {
		return ((AdmissionControllerManagementI)this.offering).demandeHebergement(requestNotificationInboundPortURI, nbCores);
	}

	/**
	 * @see fr.sorbonne_u.datacenter.admissioncontroller.interfaces.AdmissionControllerManagementI#connexionHote(String)
	 */
	@Override
	public Boolean connexionHote(String requestNotificationInboundPortURI) throws Exception {
		return ((AdmissionControllerManagementI)this.offering).connexionHote(requestNotificationInboundPortURI);
	}

	/**
	 * @see fr.sorbonne_u.datacenter.admissioncontroller.interfaces.AdmissionControllerManagementI#allouerVM(String, int)
	 */
	@Override
	public String allouerVM(String requestNotificationInboundPortURI, int nbCores) throws Exception {
		return ( ( AdmissionControllerManagementI ) this.offering ).allouerVM(requestNotificationInboundPortURI, nbCores);
	}

}
