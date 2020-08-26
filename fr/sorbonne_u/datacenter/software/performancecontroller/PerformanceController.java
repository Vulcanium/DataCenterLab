package fr.sorbonne_u.datacenter.software.performancecontroller;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.connectors.DataConnector;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.interfaces.DataRequiredI;
import fr.sorbonne_u.datacenter.hardware.processors.connectors.ProcessorManagementConnector;
import fr.sorbonne_u.datacenter.hardware.processors.interfaces.ProcessorManagementI;
import fr.sorbonne_u.datacenter.hardware.processors.ports.ProcessorManagementOutboundPort;
import fr.sorbonne_u.datacenter.interfaces.ControlledDataRequiredI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.interfaces.AdmissionControllerManagementI;
import fr.sorbonne_u.datacenter.software.admissioncontroller.ports.AdmissionControllerManagementOutboundPort;
import fr.sorbonne_u.datacenter.software.performancecontroller.interfaces.PerformanceControllerManagementI;
import fr.sorbonne_u.datacenter.software.performancecontroller.ports.PerformanceControllerManagementInboundPort;
import fr.sorbonne_u.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDataI;
import fr.sorbonne_u.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.sorbonne_u.datacenter.software.requestdispatcher.interfaces.RequestDispatcherSensorI;
import fr.sorbonne_u.datacenter.software.requestdispatcher.ports.RequestDispatcherDataOutboundPort;
import fr.sorbonne_u.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;


/**
 * La classe <code>PerformanceController</code> implemente le composant representant
 * un Controleur de Performance dans le Data Center.
 *
 * <p><strong>Description</strong></p>
 * 
 * Le Controleur de Performance est un actionneur qui va recuperer le temps moyen de son capteur <code>RequestDispatcherData</code>.
 * Ainsi, il pourra tracer un seuil a partir de ce temps, afin d'allouer/désallouer des VMs, des coeurs et/ou les fréquences de ces coeurs.
 * 
 * Le seuil est defini en fonction de la moyenne des temps entre la soumission de la requete par le <code>RequestDispatcher</code>
 * a <code>ApplicationVM</code>, et la notification de fin de traitement de cette derniere.
 * 
 * Si l'on est en-dessous du seuil, le Controleur procedera a une allocation afin d'ameliorer les performances de traitement des requetes.
 * Si l'on est au-dessus du seuil, le Controleur procedera a une desallocation afin de reduire l'utilisation des ressources superflues.
 */
public class PerformanceController
extends AbstractComponent 
implements RequestDispatcherSensorI, PerformanceControllerManagementI
{
	
	protected static final long SEUIL_MAX = 5000; // En millisecondes
    protected static final long SEUIL_MIN = 1000; // En millisecondes
    protected static final long DUREE_ENTRE_UN_AJUSTEMENT = 5000000000L; // 3s
    
    /** Possibilite de tracer, ou non, un graphe representant les performances de traitement des requetes */
    public static final boolean TRACE_GRAPH = true;
	
	/** L'URI du Controleur de performance. */
    protected String pcURI;
    
    /** L'URI des vm */
    protected String vmURI;
    
    /** Recupere les requetes dans un Future pour les traiter dès qu'elles arrivent */
    protected ScheduledFuture<?> tasksInFuture;
    
    /** Port entrant du PC pour qu'on puisse se connecter a lui */
    protected PerformanceControllerManagementInboundPort pcmip;
    
    /** Port sortant du RequestDispatcherData pour qu'il puisse se connecter au CP */
    protected RequestDispatcherDataOutboundPort requestDispatcherDataOutboundPort;
    
    protected String requestDispatcherDataInboundPortURI;
    
    /** Port sortant du RD pour qu'il puisse se connecter au CP */
    protected RequestDispatcherManagementOutboundPort rdmop;
    
    /** Port sortant du AC pour qu'il puisse se connecter au CP */
    protected AdmissionControllerManagementOutboundPort acmop;
    
    /** Nom du fichier contenant la courbe de performance */
    protected String Filename = "";
    
    /** Nombre de temps moyens recus grace au capteur RequestDispatcherData */
    public static int nbMoyRecu = 0;
    
    /** Pour regler la frequence entre chaque adaptation du seuil */
    protected Long lastAdaptation = 0L;
    
    /** Le dernier temps moyen recu grace au capteur RequestDispatcherData */
    protected double lastAVGTime = 0;
    
    /** Echelle de mesure prise pour calculer la moyenne */
    protected Integer[] echelle;
    
    /** Frequences des coeurs des Processors */
    protected Integer[] frequences;
    
    /** map associant l'URI des processeurs avec leur numero de coeurs alloues */
    protected Map<String, List<Integer>> processorCores;
    
    /** map associant l'URI des processeurs avec leur port entrant */
    protected Map<String, String> pmipURIs;
    
    /** map associant l'URI des processeurs avec leur port sortant, afin de modifier la frequence des coeurs */
    protected Map<String, ProcessorManagementOutboundPort> pmops;
    
    
    /**
     * Cree un composant de Controleur de Performance
     * 
     * @param pcURI												URI du Controleur de Performance
     * @param rdURI												URI du RequestDispatcher
     * @param pcmip												Port entrant du Controleur de Performance
     * @param admissionControllerManagementOutboundPortURI		Port sortant de l'Admission Controller
     * @param requestDispatcherManagementOutboundPortURI		Port sortant du Request Dispatcher
     * @param requestDispatcherDataInboundPortURI				URI du port entrant du capteur RequestDispatcherData
     * @param frequences										Frequences des coeurs a allouer/desallouer
     * @param processorCores									Map associant l'URI des processeurs avec leur numero de coeurs alloues
     * @param pmipURIs											Map associant l'URI des processeurs avec leur port entrant
     * @throws Exception
     */
	public PerformanceController(String pcURI, 
			String rdURI, 
			String pcmip, 
			String admissionControllerManagementOutboundPortURI, 
			String requestDispatcherManagementOutboundPortURI,
			String requestDispatcherDataInboundPortURI,
            Integer[] frequences,
            Map<String, List<Integer>> processorCores,
            Map<String, String> pmipURIs) throws Exception {
		
		super(1, 1);
		
		this.pcURI = pcURI;
		this.requestDispatcherDataInboundPortURI = requestDispatcherDataInboundPortURI;
		
		//Connexion au RD pour recuperer ses donnees

		System.out.println("PerformanceController 1");
		this.requestDispatcherDataOutboundPort = new RequestDispatcherDataOutboundPort(this,
                rdURI);
        this.addRequiredInterface(DataRequiredI.PullI.class);
        this.addOfferedInterface(DataRequiredI.PushI.class);
        this.addRequiredInterface(ControlledDataRequiredI.ControlledPullI.class);
        this.addPort(this.requestDispatcherDataOutboundPort);
        this.requestDispatcherDataOutboundPort.publishPort();
		System.out.println("PerformanceController 1.5");
 /*       this.requestDispatcherDataOutboundPort.doConnection(rddsdip,
                DataConnector.class.getCanonicalName());
 */
		System.out.println("PerformanceController 2");

        this.addRequiredInterface(RequestDispatcherManagementI.class);
        this.rdmop = new RequestDispatcherManagementOutboundPort(requestDispatcherManagementOutboundPortURI, this);
        this.addPort(this.rdmop);
        this.rdmop.publishPort();

		System.out.println("PerformanceController 3");

        this.addOfferedInterface(PerformanceControllerManagementI.class);
        this.pcmip = new PerformanceControllerManagementInboundPort(pcmip, this);
        this.addPort(this.pcmip);
        this.pcmip.publishPort();
        
		System.out.println("PerformanceController 4");
        
        pmops = new HashMap<>();
        
        System.out.println("PerformanceController 5");
        //Creation du fichier
        Filename = "./" + Filename + pcURI + "CourbePerfs.txt";
        
        System.out.println("PerformanceController 5.1");
        this.addRequiredInterface(ProcessorManagementI.class);
		
        System.out.println("PerformanceController 5.2");
        //Connexion aux processeurs pour modifier ses coeurs et frequences
        
        for (String processorURI : processorCores.keySet()) {
        	
        	System.out.println("PerformanceController 5.3");
        	String pmopURI = pcURI + processorURI + "op";
        	System.out.println("PerformanceController 5.4");
            ProcessorManagementOutboundPort pmop = new ProcessorManagementOutboundPort(pmopURI, this);
            System.out.println("PerformanceController 5.5");
            this.pmops.put(processorURI, pmop);
            System.out.println("PerformanceController 5.6");
            this.addPort(pmop);
            System.out.println("PerformanceController 5.7");
            pmop.publishPort();
            System.out.println("PerformanceController 5.8");
            String pmipURI = pmipURIs.get(processorURI);
            System.out.println("PerformanceController 5.9");
            pmop.doConnection(pmipURI, ProcessorManagementConnector.class.getCanonicalName());
            System.out.println("PerformanceController 5.99");
        }

		System.out.println("PerformanceController 6");
        
        //Connexion a l'Admission Controller
        
        this.addRequiredInterface(AdmissionControllerManagementI.class);
        this.acmop = new AdmissionControllerManagementOutboundPort(admissionControllerManagementOutboundPortURI, this);
        this.addPort(this.acmop);
        this.acmop.publishPort();
        
        if (TRACE_GRAPH) {
            FileWriter f = new FileWriter(Filename, false);
            f.close();
        }

        System.out.println("PerformanceController 7");
        
        this.frequences = frequences;

        echelle = new Integer[] { 2, 2, 2, 3, 3, 4, 5 };
        this.processorCores = processorCores;
        this.pmipURIs = pmipURIs;
        
		System.out.println("PerformanceController 10");
	}
	
	
	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	public void start() throws ComponentStartException
	{
		
		super.start();
		
		try {
			this.doPortConnection(
					this.requestDispatcherDataOutboundPort.getPortURI(),
					this.requestDispatcherDataInboundPortURI,
			        DataConnector.class.getCanonicalName());
		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
		
		lastAdaptation = System.nanoTime(); //System.nanoTime() pour avoir un temps plus precis que System.currentTimeMillis()
		
		
		this.tasksInFuture = this.scheduleTaskAtFixedRate(new ComponentI.ComponentTask() {

			@Override
			public void run() {
				
				try {
                    RequestDispatcherDataI rdds = getRDData();
                    logMessage("Performance Controller " + pcURI + " timestamp      : " + rdds.getTimeStamp());
                    logMessage("Performance Controller " + pcURI + " timestamper id : " + rdds.getTimeStamperId());
                    logMessage("Performance Controller " + pcURI + " request time average : " + rdds.getRequestProcessingAvg() + " ms");
                    boolean reequilibragePerfs = false;
                    
                    if(System.nanoTime() - lastAdaptation > DUREE_ENTRE_UN_AJUSTEMENT) {
                    	
                    	
                    	/* Si nous sommes au-dessus du seuil */
                    	
                    	if(rdds.getRequestProcessingAvg() > SEUIL_MAX) {
                    		
                    		int nbCoresToAllocate = 2;

                            if (lastAVGTime != 0) {
                                int i = (int) ((rdds.getRequestProcessingAvg() - lastAVGTime) / 1000);
                                nbCoresToAllocate = i >= echelle.length ? echelle[echelle.length - 1]
                                        : i > 0 ? echelle[i] : echelle[0];
                                System.out.println("echelle[" + i + "] = " + nbCoresToAllocate);
                            }
                            System.out.println("echelle[0] = " + nbCoresToAllocate);
                            
                            //Allocation d'une nouvelle VM
                            acmop.allouerVM(generateURI("avmnip"), nbCoresToAllocate);
                    	
                            reequilibragePerfs = true;
                            lastAdaptation = System.nanoTime();
                            lastAVGTime = rdds.getRequestProcessingAvg();
                                                
                    	}
                    	
                    	
                    	/* Si nous sommes en-dessous du seuil */
                    	
                    	if(rdds.getRequestProcessingAvg() < SEUIL_MIN) {
                    		rdmop.disconnectVM();
                    		
                    		reequilibragePerfs = true;
                            lastAdaptation = System.nanoTime();
                            lastAVGTime = rdds.getRequestProcessingAvg();
                    	}
                    	
                    }
                    
                    if (TRACE_GRAPH) { // On trace le graphe si necessaire
                        if (rdds.getRequestProcessingAvg() != 0) {
                            try {
                                FileWriter fw = new FileWriter(Filename, true);
                                if (!reequilibragePerfs)
                                    fw.write(nbMoyRecu + " " + rdds.getRequestProcessingAvg() + "\n");
                                else
                                    fw.write(nbMoyRecu + " " + rdds.getRequestProcessingAvg() + " "
                                            + rdds.getRequestProcessingAvg() + "\n");
                                nbMoyRecu++;
                                fw.close();
                            } catch (IOException exception) {
                                System.out.println("Erreur lors de l'ecriture : " + exception.getMessage());
                            }
                        }
                    }

			
				} catch(Exception e) {
					e.printStackTrace();
				}
			
			}

			@Override
			public void setOwnerReference(ComponentI owner) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public ComponentI getOwner() {
				// TODO Auto-generated method stub
				return null;
			}
			
			
		}, 1L, 1L, TimeUnit.SECONDS);
	
	}
	
	
	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	public void shutdown() throws ComponentShutdownException 
	{
		
		try {
			if (this.acmop.connected())
				this.acmop.doDisconnection();
		} catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
		
	}
	
	
	/** Recupere le capteur associe au Controleur de Performance
	 *  
	 * @return Une instance d'une classe implementant l'interface RequestDispatcherDataI
	 * @throws Exception
	 */
	public RequestDispatcherDataI getRDData() throws Exception {
		return (RequestDispatcherDataI) requestDispatcherDataOutboundPort.request();
	}
	
	
	/** Recupere le port sortant de l'AC associe au CP
	 *  
	 * @return Une instance de la classe AdmissionControllerManagementOutboundPort
	 */
	public AdmissionControllerManagementOutboundPort getACMOP() {
		return acmop;
	}
	
	
	/** Recupere le port sortant du RD associe au CP 
	 * 
	 * @return Une instance de la classe RequestDispatcherManagementOutboundPort
	 */
	public RequestDispatcherManagementOutboundPort getRDMOP() {
		return rdmop;
	}
	
	
	/** Genere une URI unique lors de l'instanciation de certains composants
	 * 
	 * @param uri	Une String contenant le prefixe de l'URI que l'on veut generer
	 * @return 		Une String contenant l'URI genere
	 */
	private String generateURI(String uri) {
		return uri + "_" + Math.random();
	}
	
	
	
	/**
	 * @see fr.sorbonne_u.datacenter.performancecontroller.interfaces.PerformanceControllerManagementI#notifyEndRequest(String)
	 */
	@Override
	public void notifyEndRequest(String[] VmURis) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @see fr.sorbonne_u.datacenter.requestdispatcher.interfaces.RequestDispatcherSensorI#acceptRequestDispatcherSensorData(String, RequestDispatcherDataI)
	 */
	@Override
	public void acceptRequestDispatcherSensorData(String requestDispatcherURI,
			RequestDispatcherDataI currentDynamicState) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
