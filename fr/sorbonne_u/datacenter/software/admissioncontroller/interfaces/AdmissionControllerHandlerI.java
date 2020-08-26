package fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces;

/**
 * L'interface <code>AdmissionControllerHandlerI</code> definit 
 * les actions de gestion fournies par le composant du Controleur d'Admission.
 */
public interface AdmissionControllerHandlerI {

	/**
	 * Alloue les VMs via le Controleur d'Admission.
	 * 
	 * @param requestNotificationInboundPortURI		URI du port entrant des notifications du composant
	 * @param nbCores								Nombre de coeurs a allouer aux VMs
	 * @return 										Un message de confirmation lorsque les VMs sont allouees
	 * @throws Exception
	 */
	public String allouerVM(String requestNotificationInboundPortURI, int nbCores) throws Exception;
	
	
	/**
	 * Demande a l'hote de l'application de connecter ses OutboundPorts. Retourne un booleen pour savoir si la connexion s'est faite, ou non.
	 * @param requestNotificationInboundPortURI
	 * @return true si les ports de l'hote sont bien connectes, false sinon.
	 * @throws Exception
	 */
	public Boolean handlerConnexionHote(String requestNotificationInboundPortURI) throws Exception;
	
}
