package fr.sorbonne_u.datacenter.software.admissioncontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.sorbonne_u.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.sorbonne_u.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.sorbonne_u.datacenter.software.admissioncontroller.connectors.AdmissionControllerManagementConnector;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.AdmissionControllerHandlerI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.ports.AdmissionControllerManagementInboundPort;
import fr.sorbonne_u.datacenter.software.admissioncontroller.ports.AdmissionControllerManagementOutboundPort;
import fr.sorbonne_u.datacenter.software.applicationvm.ApplicationVM;
import fr.sorbonne_u.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.sorbonne_u.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.sorbonne_u.datacenter.software.interfaces.RequestI;
import fr.sorbonne_u.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.sorbonne_u.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.sorbonne_u.datacenter.software.performancecontroller.PerformanceController;
import fr.sorbonne_u.datacenter.software.performancecontroller.connectors.PerformanceControllerManagementConnector;
import fr.sorbonne_u.datacenter.software.performancecontroller.ports.PerformanceControllerManagementOutboundPort;
import fr.sorbonne_u.datacenter.software.requestdispatcher.RequestDispatcher;
import fr.sorbonne_u.datacenter.software.requestdispatcher.connectors.RequestDispatcherManagementConnector;
import fr.sorbonne_u.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;
import fr.sorbonne_u.datacenterclient.requestgenerator.RequestGenerator;


/**
 * La classe <code>AdmissionController</code> est le composant instanciant les autres composants. 
 * 
 * <p><strong>Description</strong></p>
 * 
 * En fonction de la demande des differentes applications, ce composant 
 * va parcourir la liste des coeurs disponibles dans les différents processeurs. En fonction du nombre de coeurs de disponibles, ce
 * contrôleur d'admission va créer pour chaque application un générateur de requetes et un répartiteur de requetes avec le nombre de 
 * machines virtuelles adéquates. Si il n'y a pas assez de coeurs de disponibles, alors l'application n'est pas hébérgée dans le dataCenter
 */
public class AdmissionController 
extends		AbstractComponent
implements	AdmissionControllerHandlerI, RequestNotificationHandlerI, RequestSubmissionHandlerI{

	/** on fixe par une constante le nombre de coeurs */
	protected final int NUMBER_OF_CORES=4;
	
	/** on fixe par une constante le nombre de coeurs par VM */
	protected final int NBR_CORS_PER_VM=2;

	/** the URI of the component. */
	protected final String caURI = "ca" ;
	
	/** l'adresse du port d'entree pour le reguest generator de requetes */
	protected String inboundPortForRequestGeneratorURI;

	/** gestionnaire des ports d'entree du controleur d'admission */
	protected AdmissionControllerManagementInboundPort admissionControllerManagementInboundPort ;
	
	/** adresse du gestionnaire des ports d'entree du controleur d'admission */
	protected String admissionControllerManagementInboundPortURI = "acmip";
	
	/** Request Dispatcher a instancier via l'Admission Controller */
	protected RequestDispatcher rd;

	/** Les Outbounds Ports des AVMs pour leur allouer des coeurs */
	protected List<ApplicationVMManagementOutboundPort> avmopList;

	/** Une liste des URI des RequestSubmissionInboundPort des différentes AVM créées */
	protected List<String> rsipList;

	/** Une liste des URI des RequestNotificationOutboundPort des différentes AVM créées */
	protected List<String> rnopList;

	/** Une liste des URI des RequestSubmissionOutboundPort des différentes AVM créées */
	protected List<String> rsopList;


	/** Une map associant l'URI des Submission Inbounds Ports des AVMs avec leur Management Outbound respectif */
	protected Map<String, ApplicationVMManagementOutboundPort> avmopMap;


	/** Map entre les URIs des RequestDispatcher et les Outbounds Ports */
	protected Map<String, RequestDispatcherManagementOutboundPort> rdmopMap;

	/** adresse des ports entrants des services du composant computer*/
	protected String computerServicesInboundPortURI ;
	
	/** port sortant des services du composant computer*/
	protected ComputerServicesOutboundPort computerServicesOutboundPort ;

	/** composant abstractCVM*/
	protected AbstractCVM abstractCVM;
	
	/** Frequences des coeurs a allouer */
	protected Integer[] frequences;
	 
    /** map associant l'URI des processeurs avec leur port entrant **/
	protected Map<String, String> pmipURIs;
	
	

	/**
	 * Cree un composant d'Admission Controller
	 * 
	 * @param abstractCVM							Composant CVM
	 * @param computerServicesInboundPortURI		URI des ports entrants des services du Computer.
	 * @param frequences							Frequences des coeurs a allouer.
	 * @param pmipURIs								Map associant l'URI des processeurs avec leur port entrant.
	 * @throws Exception
	 */
	public AdmissionController(AbstractCVM abstractCVM,String computerServicesInboundPortURI, Integer[] frequences, Map<String, String> pmipURIs) throws Exception
	{

		super(1, 1);
		
		//frequences = new Integer[]{2, 2};
		this.frequences = frequences;
		this.pmipURIs = pmipURIs;

		//	this.addOfferedInterface(RequestDispatcherManagementI.class) ;
		this.abstractCVM= abstractCVM;
		this.computerServicesInboundPortURI = computerServicesInboundPortURI;

		this.admissionControllerManagementInboundPort = new AdmissionControllerManagementInboundPort(
				admissionControllerManagementInboundPortURI, this) ;
		this.addPort(this.admissionControllerManagementInboundPort) ;
		this.admissionControllerManagementInboundPort.publishPort() ;


		this.computerServicesOutboundPort = new ComputerServicesOutboundPort(this) ;
		this.addPort(this.computerServicesOutboundPort) ;
		this.computerServicesOutboundPort.publishPort() ;

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

		super.start() ;

		try {
			
			this.doPortConnection(
					this.computerServicesOutboundPort.getPortURI(),
					this.computerServicesInboundPortURI,
					ComputerServicesConnector.class.getCanonicalName());
			
			AllocatedCore[] ac = this.getAvailableCoresInComputers(NUMBER_OF_CORES);
			// Creation du Performance Controller
			logMessage("Creation du Controleur de Performance");
			
		//Map contenant l'URI des processeurs avec les coeurs alloues
			Map<String, List<Integer>> processorCores = new HashMap<>();
		for (int i = 0; i < ac.length; i++) {
				String processorURI = ac[i].processorURI;
				boolean isInList = processorCores.get(processorURI) != null;
				if (!isInList)
				processorCores.put(processorURI, new ArrayList<Integer>());
				processorCores.get(processorURI).add(ac[i].coreNo);
		}
		
			
			PerformanceController perfController = new PerformanceController
					(generateURI("pc"), 
					generateURI("rd"), 
					generateURI("pcmip"),
					generateURI("acmop"), 
					generateURI("rdmop") + 0, 
					generateURI("rddsdip"), 
					frequences, 
					processorCores, 
					pmipURIs);

			this.abstractCVM.addDeployedComponent(perfController);

			perfController.toggleLogging();
			perfController.toggleTracing();
			perfController.start();
			
			logMessage("Le Contrôleur d'Admission " + this.caURI + " se connecte au PerformanceController et au RequestDispatcher");
			// Connexion du Performance Controller avec l'Admission Controller
			AdmissionControllerManagementOutboundPort acmop = (AdmissionControllerManagementOutboundPort) perfController.getACMOP();
			acmop.doConnection(admissionControllerManagementInboundPort.getPortURI(), AdmissionControllerManagementConnector.class.getCanonicalName());

			// Connexion du PerformanceController avec le RequestDispatcher
			RequestDispatcherManagementOutboundPort rdmop = (RequestDispatcherManagementOutboundPort) perfController.getRDMOP();
			rdmop.doConnection(generateURI("rdmip"), RequestDispatcherManagementConnector.class.getCanonicalName());

			PerformanceControllerManagementOutboundPort pcmop = (PerformanceControllerManagementOutboundPort) rd.getPCMOP();
			logMessage(pcmop.getPortURI());
			pcmop.doConnection(generateURI("pcmip"), PerformanceControllerManagementConnector.class.getCanonicalName());


		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
		this.logMessage("Admission controller " + this.caURI + " starting.") ;


	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void	finalise() throws Exception
	{
		this.doPortDisconnection(this.computerServicesOutboundPort.getPortURI()) ;

		super.finalise() ;
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

			this.admissionControllerManagementInboundPort.unpublishPort();
			this.computerServicesOutboundPort.unpublishPort() ;

		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}

		super.shutdown();
	}


	// -------------------------------------------------------------------------
	// Component internal services
	// -------------------------------------------------------------------------


	/**
	 * Parcours les ordinateurs, afin d'allouer leur(s) coeur(s) aux AVM.
	 * 
	 * @param nbCores				Nombre de coeurs a allouer
	 * @return 						Tableau des coeurs disponibles dans les Computers
	 * @throws Exception
	 */
	public AllocatedCore[] getAvailableCoresInComputers(int nbCores) throws Exception
	{

		AllocatedCore[] availableCores = new AllocatedCore[0];
		availableCores = this.computerServicesOutboundPort.allocateCores(nbCores) ;
		if(availableCores.length >= nbCores)
			logMessage("Le controleur d'admission "+ this.caURI +" a alloue "+ availableCores.length );
		else
			availableCores=null;
		return availableCores;
		
	}

	
	/**
	 * Alloue les AVMs. Si les coeurs sont disponibles, ils vont etre allouer aux VMs pour les differentes applications.
	 * 
	 * @param requestNotificationInboundPortURI  	URI du port d'entree pour la notification des requetes.
	 * @param nbCores								Nombre de coeurs a allouer aux AVMs.
	 * @return 										Message de confirmation lorsque les VMs sont allouees.
	 * @throws Exception 
	 */
	@Override
	public String allouerVM(String requestNotificationInboundPortURI, int nbCores) throws Exception {

		this.logMessage("Le controleur d'admission " + this.caURI + " a recu une demande d'hebergement d'une application.");

		AllocatedCore[] availableCores = this.getAvailableCoresInComputers(nbCores);

		//si il y a aucun coeurs de disponibles
		if(availableCores == null) {
			return null;
		}
		Vector<ApplicationVM> avms = new Vector<>();

		//Creation de plusieurs VMs
		this.logMessage("Creation of an applicationVM...");
		String rnip = generateURI("rnip");
		int randomInt = randomInt();
		for(int i = 0 ; i<nbCores/NBR_CORS_PER_VM;i++)
		{


			String avmURI = generateURI("avmURI_" + randomInt + "_" + i);
			String rsip = generateURI("rsip"  + i);
			String avmip = generateURI("avmip"  + i);


			ApplicationVM avm = new ApplicationVM(
					avmURI,
					avmip,
					rsip,
					rnip
					);
			avms.add(avm);
			this.abstractCVM.addDeployedComponent(avm);
			avm.toggleTracing();
			avm.toggleLogging();



			//creation de la vm avec le management port 
			String avmop = generateURI("avmop"  + i);
			ApplicationVMManagementOutboundPort applicationVMManagementOutboundPort = 
					new ApplicationVMManagementOutboundPort(
							avmop, new AbstractComponent(1, 1) {});
			applicationVMManagementOutboundPort.publishPort();
			applicationVMManagementOutboundPort.doConnection(avmip,
					ApplicationVMManagementConnector.class.getCanonicalName());

			// Allocation des coeurs des computers aux AVMs
			AllocatedCore[] availableCoresTmp = new AllocatedCore[NBR_CORS_PER_VM];
			for(int j=0 ; j<NBR_CORS_PER_VM ; j++)
				availableCoresTmp[j] = availableCores[(i * NBR_CORS_PER_VM) + j];

			applicationVMManagementOutboundPort.allocateCores(availableCoresTmp);
			//		applicationVMManagementOutboundPort.allocateCores(availableCores);

			this.logMessage(availableCores.length + " cores allocated.");



		}
		String rdsip= generateURI("rdsip");
		String rgnip= generateURI("rgnip");
		String rddip= generateURI("rddip");
		String pcmop = generateURI("pcmop");

		rd=createRequestDispatcher(randomInt,avms,rdsip,rnip,rgnip, pcmop, rddip);
		RequestGenerator rg=createRequestGenerator(randomInt,rdsip,rgnip);


		//demarrage des composants 
		for(ApplicationVM  applicationVM:avms )
			applicationVM.start();
		rd.start();		
		rg.start();	


		rg.prepareGeneration();



		return "OK";

	}
	
	
	/**
	 * Methode privee qui va retourner un request Dispatcher une fois qu'il y aura des coeurs disponibles pour les 
	 * machines virtuelles.
	 * 
	 * @param randomInt entier qui va etre genere pour reconnaitre chaque request dispatcher par un identifiant unique
	 * @param avms		vecteur qui regroupe les différentes machines virtuelles
	 * @param rdsip     adresse du port entrant du request dispatcher
	 * @param rnip		adresse du port entrant de notification 
	 * @param rgnip		adresse du port entrant de notification du request generator
	 * @return 			instance du RequestDispatcher
	 * @throws Exception
	 */
	private RequestDispatcher createRequestDispatcher(int randomInt,Vector<ApplicationVM> avms,String rdsip,String rnip,String rgnip, String pcmop, String rddip) throws Exception
	{
		// Creation d'un requestdispatcher
		this.logMessage("Creation of the requestDispatcher...");


		String rdURI = "RequestDispatcher_" + randomInt;
		String rdmip = generateURI("rdmip");

		RequestDispatcher rd = new RequestDispatcher(rdURI, rdsip, rdmip, avms, rnip, rgnip, pcmop, rddip);

		this.abstractCVM.addDeployedComponent(rd);
		rd.toggleTracing() ;
		rd.toggleLogging() ;

		// Creation du management outbound port pour le RD
		RequestDispatcherManagementOutboundPort rdop = new RequestDispatcherManagementOutboundPort(this) ;
		this.addPort(rdop) ;
		rdop.publishPort() ;

		this.doPortConnection(
				rdop.getPortURI(),
				rdmip,
				RequestDispatcherManagementConnector.class.getCanonicalName()) ;
		return rd;
	}

	/**
	 * Methode privee qui permet de creer un request generator qui sera associe a un request dispatcher de particulier pour 
	 * traiter les requetes d'une application
	 * 
	 * @param randomInt entier qui va etre genere pour reconnaitre chaque request generator par un identifiant unique
	 * @param rdsip 	adresse du port d'entree du request dispatcher 
	 * @param rgnip 	adresse du port d'entree de notification
	 * @return 			instance du RequestGenerator
	 * @throws Exception
	 */
	private RequestGenerator createRequestGenerator(int randomInt,String rdsip,String rgnip) throws Exception
	{
		// Creation du Request Generator
		String rgmop = generateURI("rgmop");
		String rgmip = generateURI("rgmip");
		String rgURI = "RequestGenerator_" + randomInt;
		RequestGenerator rg = new RequestGenerator(rgURI,500.0,6000000000L,rgmip,rdsip,rgmop,rgnip) ;
		this.abstractCVM.addDeployedComponent(rg);

		rg.toggleTracing() ;
		rg.toggleLogging() ;



		return rg;
	}
	
	
	@Override
	public Boolean handlerConnexionHote(String requestNotificationInboundPortURI) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	/**
	 * Renvoie un nombre au hasard qui va servir d'identifiant aux differents composants
	 * @return Identifiant unique pour le composant
	 */
	private int randomInt()
	{
		return (int) Math.floor(Math.random() * 100);
	}

	
	/** Genere une URI unique lors de l'instanciation de certains composants
	 * 
	 * @param uri	Une String contenant le prefixe de l'URI que l'on veut generer
	 * @return 		Une String contenant l'URI genere
	 */
	private String generateURI(String uri) {
		return uri + "_" + Math.random();
	}


	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {
		// TODO Auto-generated method stub

	}


	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		// TODO Auto-generated method stub

	}


	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		// TODO Auto-generated method stub

	}

}
