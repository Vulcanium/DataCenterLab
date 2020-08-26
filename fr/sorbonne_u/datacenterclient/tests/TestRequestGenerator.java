package fr.sorbonne_u.datacenterclient.tests;

//Copyright Jacques Malenfant, Sorbonne Universite.
//
//Jacques.Malenfant@lip6.fr
//
//This software is a computer program whose purpose is to provide a
//basic component programming model to program with components
//distributed applications in the Java programming language.
//
//This software is governed by the CeCILL-C license under French law and
//abiding by the rules of distribution of free software.  You can use,
//modify and/ or redistribute the software under the terms of the
//CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
//URL "http://www.cecill.info".
//
//As a counterpart to the access to the source code and  rights to copy,
//modify and redistribute granted by the license, users are provided only
//with a limited warranty  and the software's author,  the holder of the
//economic rights,  and the successive licensors  have only  limited
//liability. 
//
//In this respect, the user's attention is drawn to the risks associated
//with loading,  using,  modifying and/or developing or reproducing the
//software by the user in light of its specific status of free software,
//that may mean  that it is complicated to manipulate,  and  that  also
//therefore means  that it is reserved for developers  and  experienced
//professionals having in-depth computer knowledge. Users are therefore
//encouraged to load and test the software's suitability as regards their
//requirements in conditions enabling the security of their systems and/or 
//data to be ensured and,  more generally, to use and operate it in the 
//same conditions as regards security. 
//
//The fact that you are presently reading this means that you have had
//knowledge of the CeCILL-C license and that you accept its terms.

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.datacenter.hardware.computers.Computer;
import fr.sorbonne_u.datacenter.hardware.processors.Processor;
import fr.sorbonne_u.datacenter.hardware.tests.ComputerMonitor;
import fr.sorbonne_u.datacenter.software.applicationvm.ApplicationVM;
import fr.sorbonne_u.datacenter.software.requestdispatcher.RequestDispatcher;
import fr.sorbonne_u.datacenterclient.requestgenerator.RequestGenerator;

/**
 * The class <code>TestRequestGenerator</code> deploys a test application for
 * request generation in a single JVM (no remote execution provided) for a data
 * center simulation.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p>
 * A data center has a set of computers, each with several multi-core
 * processors. Application virtual machines (AVM) are created to run
 * requests of an application. Each AVM is allocated cores of different
 * processors of a computer. AVM then receive requests for their application.
 * See the data center simulator documentation for more details about the
 * implementation of this simulation.
 * </p>
 * <p>
 * This test creates one computer component with two processors, each having
 * two cores. It then creates an AVM and allocates it all four cores of the
 * two processors of this unique computer. A request generator component is
 * then created and linked to the application virtual machine.  The test
 * scenario starts the request generation, wait for a specified time and then
 * stops the generation. The overall test allots sufficient time to the
 * execution of the application so that it completes the execution of all the
 * generated requests.
 * </p>
 * <p>
 * The waiting time in the scenario and in the main method must be manually
 * set by the tester.
 * </p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : May 5, 2015</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				TestRequestGenerator
extends		AbstractCVM
{
	/*
	Par exemple la VM a un port de type RequestSubmissionInboundPort son uri sera rsibp 
	que tu passera par le constructeur. Et dans le constructeur  de la classe VM tu 
	vas cr�er  ton Port RequestSubmissionInboundPort rsop = 
	new RequestSubmissionInboundPort("rsibp" ....
			Et le Request dispatcher a un port RequestSubmissionOutBondPort que tu vas 
			relier avec "rsibp" cr�er pr�c�demment en faisant doConnection*/
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
	public static final String 	PerformanceControllerManagementOutboundPortURI = "rddip" ;
	public static final String 	RequestDispatcherDataInboundPortURI = "rddip" ;

	/** 	Computer monitor component.										*/
	protected ComputerMonitor						cm ;
	/** 	Request dispatcher component.							*/
	protected RequestDispatcher							rd ;	
	/** 	Application virtual machine component.							*/
	//	protected ApplicationVM							vm ;
	/** 	Request generator component.										*/
	protected RequestGenerator						rg ;
	/** Integrator component.											*/
	protected Integrator integ ;

	private final int NUMBER_OF_VM =3;

	protected Vector<ApplicationVM> applicationsVMs = new Vector<ApplicationVM>(NUMBER_OF_VM);

	private static Computer c;
	// ------------------------------------------------------------------------
	// Component virtual machine constructors
	// ------------------------------------------------------------------------

	public				TestRequestGenerator()
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
		String computerURI = "computer0" ;
		int numberOfProcessors = 3 ;
		int numberOfCores = 4 ;
		Set<Integer> admissibleFrequencies = new HashSet<Integer>() ;
		admissibleFrequencies.add(1500) ;	// Cores can run at 1,5 GHz
		admissibleFrequencies.add(3000) ;	// and at 3 GHz
		Map<Integer,Integer> processingPower = new HashMap<Integer,Integer>() ;
		processingPower.put(1500, 1500000) ;	// 1,5 GHz executes 1,5 Mips
		processingPower.put(3000, 3000000) ;	// 3 GHz executes 3 Mips
		 c = new Computer(
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
				ComputerDynamicStateDataInboundPortURI) ;//on doit envoyer ce port inboud au controleur d'admission pour le lier avec un outbondPort
		this.addDeployedComponent(c) ;
		c.toggleLogging() ;
		c.toggleTracing() ;
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
		// --------------------------------------------------------------------

		// --------------------------------------------------------------------
		// Create an Request dispatcher component
		// --------------------------------------------------------------------
		this.rd =  new RequestDispatcher("rd0",	// Request dispatcher component URI
				InboundPortForRequestDispatcherURI,
				RequestDispatcherManagementInboundPortURI,
				this.applicationsVMs,
				RequestNotificationInboundPortURI,
				"test",
				PerformanceControllerManagementOutboundPortURI,
				RequestDispatcherDataInboundPortURI) ;
		this.addDeployedComponent(this.rd) ;
		// Toggle on tracing and logging in the request dispatcher to
		// follow the execution of individual requests.
		this.rd.toggleTracing() ;
		this.rd.toggleLogging() ;
		// --------------------------------------------------------------------


		ApplicationVM vm ;
		for(int i =0 ; i<NUMBER_OF_VM ; i++)
		{
			vm = new ApplicationVM("vm" + i,	// application vm component URI
					ApplicationVMManagementInboundPortURI + i  ,
					InboundPortApplicationVMURI + i ,
					RequestNotificationInboundPortURI ) ;
			applicationsVMs.add(vm);
			this.addDeployedComponent(vm) ;
			// Toggle on tracing and logging in the application virtual machine to
			// follow the execution of individual requests.
			vm.toggleTracing() ;
			vm.toggleLogging() ;
			
		}




		// --------------------------------------------------------------------

		// --------------------------------------------------------------------
		// Creating the request generator component.
		// --------------------------------------------------------------------
//		this.rg = new RequestGenerator(
//				"rg",			// generator component URI
//				500.0,			// mean time between two requests
//				6000000000L,	// mean number of instructions in requests
//				RequestGeneratorManagementInboundPortURI,
//				InboundPortForRequestDispatcherURI
//				) ;
		this.addDeployedComponent(rg) ;

		// Toggle on tracing and logging in the request generator to
		// follow the submission and end of execution notification of
		// individual requests.
		this.rg.toggleTracing() ;
		this.rg.toggleLogging() ;
		// --------------------------------------------------------------------

		// --------------------------------------------------------------------
		// Creating the integrator component.
		// --------------------------------------------------------------------
			this.integ = new Integrator(
				ComputerServicesInboundPortURI,
				this.applicationsVMs,
				RequestDispatcherManagementInboundPortURI,
				RequestGeneratorManagementInboundPortURI) ;
		this.addDeployedComponent(this.integ) ;
		/*Integrator integ;
		for(ApplicationVM vm2 : this.applicationsVMs)
		{
			integ = new Integrator(
					ComputerServicesInboundPortURI,
					vm2.getApplicationVMManagementInboundPortURI(),
					RequestDispatcherManagementInboundPortURI,
					RequestGeneratorManagementInboundPortURI) ;
			this.addDeployedComponent(integ) ;
		}*/
		// --------------------------------------------------------------------

		// complete the deployment at the component virtual machine level.
		super.deploy();
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
			final TestRequestGenerator trg = new TestRequestGenerator() ;
			trg.startStandardLifeCycle(10000L) ;
			
			// Augment the time if you want to examine the traces after
			// the execution of the program.
			
			Thread.sleep(10000L) ;
			//Thread.sleep(50000L) ;
			
			
			
			// Exit from Java (closes all trace windows...).
			//	System.exit(0) ;
		} catch (Exception e) {
			throw new RuntimeException(e) ;
		}
		System.out.println(c.getDynamicState());
	}

	@Override
	public boolean		startStandardLifeCycle(long duration)
	{
		try {
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
