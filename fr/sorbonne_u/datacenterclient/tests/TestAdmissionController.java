package fr.sorbonne_u.datacenterclient.tests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.datacenter.hardware.computers.Computer;
import fr.sorbonne_u.datacenter.hardware.processors.Processor;
import fr.sorbonne_u.datacenter.hardware.tests.ComputerMonitor;
import fr.sorbonne_u.datacenter.software.admissioncontroller.AdmissionController;
import fr.sorbonne_u.datacenter.software.applicationvm.ApplicationVM;
import fr.sorbonne_u.datacenter.software.requestdispatcher.RequestDispatcher;
import fr.sorbonne_u.datacenterclient.requestgenerator.RequestGenerator;

/**
 * La classe <code>TestAdmissionController</code> deploie une application de test 
 * en instanciant les composants necessaires au bon fonctionnement du Data Center dans une seule JVM.
 * 
 * <p><strong>Description</strong></p>
 * 
 * Le Test Admission Controller utilise des elements du <code>TestRequestGenerator</code> pour lancer la simulation.
 * Le nouveau programme principal pour lancer l'execution du Data Center est donc le <code>TestAdmissionController</code>.
 */
public class				TestAdmissionController
extends		AbstractCVM
{
	
	// ------------------------------------------------------------------------
	// Constants and instance variables
	// ------------------------------------------------------------------------

	// Predefined URI of the different ports visible at the component assembly
	// level.
	public static final String	ComputerServicesInboundPortURI = "cs-ibp" ;
	public static final String	ComputerStaticStateDataInboundPortURI = "css-dip" ;
	public static final String	ComputerDynamicStateDataInboundPortURI = "cds-dip" ;
	public static final String	ApplicationVMManagementInboundPortURI = "avm-ibp" ;
	public static final String	RequestDispatcherManagementInboundPortURI = "rdmip" ;
	public static final String	InboundPortForRequestDispatcherURI = "rsibp" ;
	public static final String	InboundPortApplicationVMURI = "rsibp2" ;
	public static final String	RequestSubmissionOutboundPortURI = "rsobp" ;
	public static final String	RequestNotificationInboundPortURI = "rnibp" ;
	public static final String	RequestGeneratorManagementInboundPortURI = "rgmip" ;

	/** 	Computer monitor component.										*/
	protected ComputerMonitor						cm ;

	protected RequestGenerator						rg ;

	/** 	Request dispatcher component.							*/
	protected RequestDispatcher							rd ;
	/** 	Admission controller component.							*/
	protected AdmissionController admissionController;	 
	/** 	Application virtual machine component.							*/
	protected ApplicationVM							vm ;
	/** 	Request generator component.										*/
	/** Integrator component.											*/
	protected Integrator integ ;


	private final int NUMBER_OF_COMPUTER =1;

	private static Computer computer;
	// ------------------------------------------------------------------------
	// Component virtual machine constructors
	// ------------------------------------------------------------------------

	public				TestAdmissionController()
			throws Exception
	{
		super();


	}

	// ------------------------------------------------------------------------
	// Component virtual machine methods
	// ------------------------------------------------------------------------

	@Override
	public void			deploy() throws Exception
	{
		Processor.DEBUG = true ;

		// --------------------------------------------------------------------
		// Create and deploy a computer component with its 2 processors and
		// each with 2 cores.
		// --------------------------------------------------------------------

		createComputers();

		this.admissionController = new AdmissionController(this,this.ComputerServicesInboundPortURI, null, null) ;
		this.addDeployedComponent(this.admissionController) ;


		// complete the deployment at the component virtual machine level.
		super.deploy();
	}





	private void createComputers() throws Exception {
		// TODO Auto-generated method stub
		String computerURI = "computer0" ;
		int numberOfProcessors = 5 ;
		int numberOfCores = 2 ;
		Set<Integer> admissibleFrequencies = new HashSet<Integer>() ;
		admissibleFrequencies.add(1500) ;	// Cores can run at 1,5 GHz
		admissibleFrequencies.add(3000) ;	// and at 3 GHz
		Map<Integer,Integer> processingPower = new HashMap<Integer,Integer>() ;
		processingPower.put(1500, 1500000) ;	// 1,5 GHz executes 1,5 Mips
		processingPower.put(3000, 3000000) ;	// 3 GHz executes 3 Mips
		computer  = new Computer(
				computerURI,
				admissibleFrequencies,
				processingPower,  
				1500,		// Test scenario 1, frequency = 1,5 GHz
				// 3000,	// Test scenario 2, frequency = 3 GHz
				1500,		// max frequency gap within a processor
				numberOfProcessors,
				numberOfCores,
				ComputerServicesInboundPortURI,
				ComputerStaticStateDataInboundPortURI,
				ComputerDynamicStateDataInboundPortURI) ;
		this.addDeployedComponent(computer) ;
		computer.toggleLogging() ;
		computer.toggleTracing() ;
		// --------------------------------------------------------------------

		// --------------------------------------------------------------------
		// Create the computer monitor component and connect its to ports
		// with the computer component.
		// --------------------------------------------------------------------
		this.cm = new ComputerMonitor(computerURI,
				true,
				ComputerStaticStateDataInboundPortURI,
				ComputerDynamicStateDataInboundPortURI) ;
		this.addDeployedComponent(this.cm) ;

	}

	
	
	// ------------------------------------------------------------------------
	// Test scenarios and main execution.
	// ------------------------------------------------------------------------

	/**
	 * execute the test application.
	 * 
	 * @param args	command line arguments, disregarded here.
	 * @throws Exception 
	 */
	public static void	main(String[] args) throws Exception
	{
		// Uncomment next line to execute components in debug mode.
		// AbstractCVM.toggleDebugMode() ;
		
		try {
			System.out.println("Start test with AdmissionController");
			final TestAdmissionController trg = new TestAdmissionController() ;
			trg.startStandardLifeCycle(100L) ;
			
		if(	trg.admissionController.allouerVM("", 6) == null)
			System.out.println("Désolé! pas de coeurs disponibles");
		
		if(	trg.admissionController.allouerVM("", 4) == null)
			System.out.println("Désolé! pas de coeurs disponibles");


			// Augment the time if you want to examine the traces after
			// the execution of the program.
			Thread.sleep(10000L) ;
			// Exit from Java (closes all trace windows...).
			//	System.exit(0) ;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e) ;
		}
	}

	@Override
	public boolean		startStandardLifeCycle(long duration)
	{
		try {

			System.out.println("Le test du Controleur d'Admission est démarré !");

			assert	duration	> 0 ;
			this.deploy() ;
			System.out.println("starting...") ;
			this.start() ;
			System.out.println("executing...") ;
			this.execute() ;
			Thread.sleep(duration) ;
			return true ;
		} catch (Exception e) {
			e.printStackTrace() ;
			return false ;
		}
	}
}
