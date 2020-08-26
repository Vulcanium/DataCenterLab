package fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces;

import fr.sorbonne_u.components.interfaces.OfferedI;
import fr.sorbonne_u.components.interfaces.RequiredI;

/**
 * L'interface <code>AdmissionControllerManagementI</code> definit 
 * les actions de gestion fournies par le composant du Controleur d'Admission.
 */
public interface AdmissionControllerManagementI 
extends OfferedI, RequiredI {
	
	/**
	 * Demande l'hebergement d'une application. Le Request Dispatcher retournera l'URI de son RequestSubmissionInboundPort.
	 * @param requestNotificationInboundPortURI
	 * @return l'URI du RequestSubmissionInboundPort.
	 * @throws Exception
	 */
	public String demandeHebergement(String requestNotificationInboundPortURI, int nbCores) throws Exception;
	
	
	/**
	 * Demande a l'hote de l'application de connecter ses OutboundPorts. Retourne un bool�en pour savoir si la connexion s'est faite, ou non.
	 * @param requestNotificationInboundPortURI
	 * @return true si les ports de l'Host sont bien connectes, false sinon.
	 * @throws Exception
	 */
	public Boolean connexionHote(String requestNotificationInboundPortURI) throws Exception;
	
	
	/**
	 * Alloue les VMs via le Controleur d'Admission.
	 * 
	 * @param requestNotificationInboundPortURI		URI du port entrant des notifications du composant
	 * @param nbCores								Nombre de coeurs a allouer aux VMs
	 * @return 										Un message de confirmation lorsque les VMs sont allouees
	 * @throws Exception
	 */
	public String allouerVM(String requestNotificationInboundPortURI, int nbCores) throws Exception;
	
}
