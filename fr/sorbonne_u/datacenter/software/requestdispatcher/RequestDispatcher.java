package fr.sorbonne_u.datacenter.software.requestdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.datacenter.interfaces.ControlledDataOfferedI;
import fr.sorbonne_u.datacenter.software.applicationvm.ApplicationVM;
import fr.sorbonne_u.datacenter.software.connectors.RequestNotificationConnector;
import fr.sorbonne_u.datacenter.software.connectors.RequestSubmissionConnector;
import fr.sorbonne_u.datacenter.software.interfaces.RequestI;
import fr.sorbonne_u.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.sorbonne_u.datacenter.software.interfaces.RequestNotificationI;
import fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionI;
import fr.sorbonne_u.datacenter.software.performancecontroller.interfaces.PerformanceControllerManagementI;
import fr.sorbonne_u.datacenter.software.performancecontroller.ports.PerformanceControllerManagementOutboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestNotificationInboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.sorbonne_u.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.sorbonne_u.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDataI;
import fr.sorbonne_u.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.sorbonne_u.datacenter.software.requestdispatcher.ports.RequestDispatcherDataInboundPort;
import fr.sorbonne_u.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementInboundPort;
import fr.sorbonne_u.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;

/**
 * La classe <code>RequestDispatcher</code> implemente le composant representant 
 * un repartiteur de requetes dans le Data Center. 
 * 
 * <p><strong>Description</strong></p>
 * 
 * Ce composant est responsable de la repartition des differentes requetes vers les differentes machines virtuelles.
 */
public class RequestDispatcher
extends		AbstractComponent
implements	RequestNotificationHandlerI,RequestDispatcherManagementI,RequestSubmissionHandlerI
{
	public static int	DEBUG_LEVEL = 2 ;

	// -------------------------------------------------------------------------
	// Constants and instance variables
	// -------------------------------------------------------------------------
	
	/** compteur qui differencie les machines virtuelles */
	private int compteur=-1;
	
	/** the URI of the component.											*/
	protected final String					rdURI ;

	/** the outbound port provided to manage the component.					*/
	protected RequestDispatcherManagementInboundPort requestDispatcherManagementInboundPort ;
	
	/** gestionnaire des ports de sorties du request dispatcher*/
	protected RequestDispatcherManagementOutboundPort	requestDispatcherManagementOutboundPort ;
	
	/** adresse du gestionnaire des ports d'entrees du request dispatcher */
	protected String requestDispatcherManagementInboundPortURI;
	
	/** port de sortie de notification du request dispatcher*/
	protected RequestNotificationOutboundPort requestNotificationOutboundPort ;
	
	/** port d'entree de notification du request dispatcher */
	protected RequestNotificationInboundPort requestNotificationInboundPort ;


	/** the input port used to receive requests to the service provider.		*/
	protected RequestSubmissionInboundPort	InboundPortForRequestDispatcher ;
	
	/** adresse du port d'entree du request dispatcher*/
	protected String InboundPortForRequestDispatcherURI;
	
	/** adresse du port d'entree du generateur de requetes */
	protected String InboundPortForRequestGeneratorURI;

	/** adresse du port d'entree de la machine virtuelle*/
	protected String InboundPortApplicationVMURI;	

	/** port de soumission de sortie du request dispatcher */
	protected RequestSubmissionOutboundPort	requestSubmissionOutboundPort ;
	
	//pour communiquer avec le requestGenerator
	
	/** adresse de notification du port d'entree du request dispatcher */
	protected String requestNotificationInboundPortURI ;
	
	/** adresse de notification du port d'entree du request generator */
	protected String requestGeneratorNotificationInboundPortURI ;
	
	/** port de notification d'entrée du request dispatcher*/
	protected RequestNotificationInboundPort rnip;
	
	/** le vecteur regroupant les differentes machines virtuelles */
	protected Vector<ApplicationVM> applicationsVMs ;
	
	
	/** Pour controler le nombre de requetes soumises par le RD, afin de fixer le seuil (a savoir, le temps moyen) */
	public static final int NB_REQUEST = 5;
	
	
	/** Map associant l'URI de la requete avec le temps de depart de la requete (en millisecondes) */
	protected Map<String , Long> requestStartTimes;
	
	/** Liste recuperant le temps fin des requetes (en millisecondes) */
	protected List<RequestTime> requestEndTimes;
	
	protected PerformanceControllerManagementOutboundPort performanceControllerManagementOutboundPort;
	protected RequestDispatcherDataInboundPort requestDispatcherDataInboundPort;
	
	
	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Cree le composant du Request Dispatcher
	 * 
	 * @param rdURI												URI du composant du Request Dispatcher
	 * @param InboundPortForRequestDispatcherURI				URI du port d'entree du Request Dispatcher
	 * @param requestDispatcherManagementInboundPortURI			URI du gestionnaire des ports d'entree du Request Dispatcher
	 * @param applicationsVMs									Tableau qui regroupe les differentes machines virtuelles
	 * @param requestNotificationInboundPortURI					URI du port d'entree de notification du Request Dispatcher
	 * @param requestGeneratorNotificationInboundPortURI		URI du port d'entree de notification du Request Generator
	 * @param performanceControllerManagementOutboundPortURI	URI du port de sortie du Performance Controller
	 * @param requestDispatcherDataInboundPortURI				URI du port d'entree du RequestDispatcherData
	 * @throws Exception
	 */
	public	RequestDispatcher(String rdURI,
			String InboundPortForRequestDispatcherURI,
			String requestDispatcherManagementInboundPortURI,
			Vector<ApplicationVM> applicationsVMs,
			String requestNotificationInboundPortURI,
			String requestGeneratorNotificationInboundPortURI,
			String performanceControllerManagementOutboundPortURI,
			String requestDispatcherDataInboundPortURI
			) throws Exception
	{
		super(1, 1) ;

		assert rdURI != null;
		assert InboundPortForRequestDispatcherURI != null;
		assert InboundPortApplicationVMURI != null;
		assert requestDispatcherManagementInboundPortURI != null;
		assert applicationsVMs != null;
		assert requestNotificationInboundPortURI != null ;

		
		
		this.applicationsVMs=applicationsVMs;

		// initialization
		this.rdURI = rdURI ;
		this.requestNotificationInboundPortURI = requestNotificationInboundPortURI ;
		this.requestGeneratorNotificationInboundPortURI=requestGeneratorNotificationInboundPortURI;
		//this.requestSubmissionInboundPortSecondeLevelURI=requestSubmissionInboundPortSecondeLevelURI;

		this.addOfferedInterface(RequestSubmissionI.class) ;
		this.InboundPortForRequestDispatcher =new RequestSubmissionInboundPort(
				InboundPortForRequestDispatcherURI, this) ;
		this.addPort(this.InboundPortForRequestDispatcher) ;
		this.InboundPortForRequestDispatcher.publishPort() ;

		this.addOfferedInterface(RequestSubmissionI.class) ;
		this.requestSubmissionOutboundPort =new RequestSubmissionOutboundPort( this) ;
		this.addPort(this.requestSubmissionOutboundPort) ;
		this.requestSubmissionOutboundPort.publishPort() ;


		this.addOfferedInterface(RequestDispatcherManagementI.class) ;
		this.requestDispatcherManagementInboundPort =new RequestDispatcherManagementInboundPort(
				requestDispatcherManagementInboundPortURI, this) ;
		this.addPort(this.requestDispatcherManagementInboundPort) ;
		this.requestDispatcherManagementInboundPort.publishPort() ;
		
		/* ports servant à notifier les autres composants*/
		this.requestNotificationOutboundPort = new RequestNotificationOutboundPort(this) ;
		this.addPort(this.requestNotificationOutboundPort) ;
		this.requestNotificationOutboundPort.publishPort() ;
	
		
		this.addOfferedInterface(RequestNotificationI.class) ;
		this.rnip =new RequestNotificationInboundPort(requestNotificationInboundPortURI, this) ;
		this.addPort(this.rnip) ;
		this.rnip.publishPort() ;
		
		this.addRequiredInterface(PerformanceControllerManagementI.class);
		this.performanceControllerManagementOutboundPort = new PerformanceControllerManagementOutboundPort(
				performanceControllerManagementOutboundPortURI, this);
		this.addPort(this.performanceControllerManagementOutboundPort);
		this.performanceControllerManagementOutboundPort.publishPort();
		
		this.addOfferedInterface( ControlledDataOfferedI.ControlledPullI.class );
		this.requestDispatcherDataInboundPort = new RequestDispatcherDataInboundPort(
				requestDispatcherDataInboundPortURI , this );
		this.addPort( this.requestDispatcherDataInboundPort );
		this.requestDispatcherDataInboundPort.publishPort();
		
		requestStartTimes = new HashMap<>();
		requestEndTimes = new ArrayList<>();
	}

	// -------------------------------------------------------------------------
	// Component life-cycle
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public void	start() throws ComponentStartException
	{

		super.start();
		this.logMessage("Request dispatcher " + this.rdURI + " starting.") ;
		try {
			this.doPortConnection(
					this.requestNotificationOutboundPort.getPortURI(),
					this.requestGeneratorNotificationInboundPortURI,
					RequestNotificationConnector.class.getCanonicalName()) ;
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
		
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void	finalise() throws Exception
	{
		
		super.finalise();
		
	}

	/**
	 * shut down the component.
	 * 

	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void	shutdown() throws ComponentShutdownException
	{

		try {
			this.requestDispatcherManagementInboundPort.unpublishPort() ;
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}

		super.shutdown();
	}

	// -------------------------------------------------------------------------
	// Component internal services
	// -------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionHandlerI#acceptRequestSubmissionAndNotify(fr.sorbonne_u.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {

		this.logMessage(this.rdURI + " redirection request " + r.getRequestURI());
					
		if(compteur >= this.applicationsVMs.size()-1)
			compteur=0;
		else
			compteur++;
				
		ApplicationVM vm = this.applicationsVMs.get(compteur);
		
		this.doPortConnection(
				this.requestSubmissionOutboundPort.getPortURI(),
				vm.getRequestSubmissionInboundPortURI(),
				RequestSubmissionConnector.class.getCanonicalName()) ;
		
		//On commence a calculer le temps d'execution de la requete, a partir du moment ou on la soumet a la VM
		requestStartTimes.put( r.getRequestURI() , System.nanoTime() );
		
		this.requestSubmissionOutboundPort.submitRequestAndNotify(r) ;

	}

	/**
	 * @see fr.sorbonne_u.datacenter.software.interfaces.RequestNotificationHandlerI#acceptRequestSubmissionAndNotify(fr.sorbonne_u.datacenter.software.interfaces.RequestI)
	 */
	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		
		
		this.requestNotificationOutboundPort.notifyRequestTermination(r);
		
		this.logMessage("RD "+ this.rdURI +
				"est notifié que la requête "+ r.getRequestURI() +
				" a terminé et notifie le RG.");
		
		//On recupere le temps d'execution de la requete
		requestEndTimes.add( new RequestTime( r.getRequestURI() , System.nanoTime() ) ); //System.nanoTime() permet d'avoir un resultat plus pertinent que System.currentTimeMillis()
		
		changeStateOfRequest();
	}
	
	/**
	 * @see fr.sorbonne_u.datacenter.requestdispatcher.interfaces.RequestDispatcherManagementI#disconnectVM()
	 */
	public void disconnectVM() throws Exception {
		
		ApplicationVM vm_to_disconnect = applicationsVMs.remove(0);
		this.doPortDisconnection(vm_to_disconnect.getRequestSubmissionInboundPortURI());
		
	}
	
	
	/**
	 * Supprime la premiere requete de requestEndTimes, et recupere son URI afin d'identifier la requete a supprimer dans requestStartTimes.
	 * Ainsi, cette requete ne sera plus prise en compte dans le prochain calcul du seuil.
	 */
	private void changeStateOfRequest() {
		
		if ( requestEndTimes.size() > NB_REQUEST ) {
			String uri = requestEndTimes.remove(0).requestURI;
			requestStartTimes.remove( uri );
		}

	}
	
	/**
	 * La classe privee <code>RequestTime</code> est l'implementation 
	 * de la structure de donnees du temps de traitement d'une requete.
	 */
	private class RequestTime {

		/** URI de la requete */
		public String requestURI;
		
		/** Temps de la requete */
		public Long   time;

		/**
		 * Cree la structure de donnees du temps de traitement d'une requete.
		 * 
		 * @param requestURI	URI de la requete.
		 * @param time			Temps de la requete.
		 */
		public RequestTime( String requestURI , Long time ) {
			this.requestURI = requestURI;
			this.time = time;
		}

	}
	
	
	/**
	 * Calcule le temps moyen de traitement des requetes.
	 * Il s'agit de la duree moyenne entre la soumission des requetes du <code>RequestDispatcher</code>
	 * aux differentes <code>ApplicationVM</code>, et la notification de celles-ci au RD.
	 * 
	 * @return Temps moyen de traitement des requetes, sous la forme d'une classe instanciant l'interface <code>RequestDispatcherDataI</code>.
	 * @throws Exception
	 */
	public RequestDispatcherDataI getRequestProcessingTimeAvg() throws Exception {
		long total = 0;
		long nbRequest = 0;
		int i = 0;
		ListIterator<RequestTime> it = requestEndTimes.listIterator( requestEndTimes.size() );
		while ( i < NB_REQUEST && it.hasPrevious() ) {
			RequestTime endRequest = it.previous();
			long startTime = requestStartTimes.get( endRequest.requestURI );
			total += endRequest.time - startTime;
			nbRequest++;
		}

		long tempsMoyen = nbRequest == 0 ? 0 : total / nbRequest;
		return new RequestDispatcherData( this.rdURI , tempsMoyen / 1000000 );

	}
	
	
	/**
	 * Recupere le port sortant du Performance Controller connecte au RD
	 * 
	 * @return	Port sortant du Performance Controller connecte au RD
	 */
	public PerformanceControllerManagementOutboundPort getPCMOP() {
		return performanceControllerManagementOutboundPort;
	}
	

	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {
		// TODO Auto-generated method stub

	}

}
