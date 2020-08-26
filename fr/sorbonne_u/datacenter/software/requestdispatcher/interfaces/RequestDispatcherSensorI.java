package fr.sorbonne_u.datacenter.software.requestdispatcher.interfaces;

/**
 * L'interface <code>RequestDispatcherSensorI</code> definit 
 * les actions de gestion des donnees dynamiques fournies par le composant du RequestDispatcherData.
 */
public interface RequestDispatcherSensorI {
	
	/**
	 * Recupere les donnees dynamiques fournies par le Request Dispatcher
	 * 
	 * @param requestDispatcherURI	URI du Request Dispatcher dont il faut recuperer les donnees dynamiques
	 * @param currentDynamicState
	 * @throws Exception
	 */
	public void acceptRequestDispatcherSensorData(String requestDispatcherURI ,
            RequestDispatcherDataI currentDynamicState ) throws Exception;

}
