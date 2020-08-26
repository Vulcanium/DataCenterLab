package fr.sorbonne_u.datacenter.software.requestdispatcher.interfaces;

import fr.sorbonne_u.components.interfaces.DataOfferedI;
import fr.sorbonne_u.components.interfaces.DataRequiredI;
import fr.sorbonne_u.datacenter.interfaces.TimeStampingI;

/**
 * L'interface <code>RequestDispatcherDataI</code> definit 
 * les actions de gestion fournies par le composant <code>RequestDispatcherData</code>.
 */
public interface RequestDispatcherDataI 
extends DataOfferedI.DataI, DataRequiredI.DataI, TimeStampingI {

	/**
	 * Recupere l'URI du Request Dispatcher
	 * 
	 * @return	URI du Request Dispatcher
	 */
	public String getRequestDispatcherURI();

	/**
	 * Recupere le temps moyen de traitement des requetes
	 * 
	 * @return	Le temps moyen de traitement des requetes
	 */
    public double getRequestProcessingAvg();	
}
