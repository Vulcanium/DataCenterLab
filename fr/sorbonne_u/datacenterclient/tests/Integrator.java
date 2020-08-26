package fr.sorbonne_u.datacenterclient.tests;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

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

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.sorbonne_u.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.sorbonne_u.datacenter.hardware.computers.interfaces.ComputerServicesI;
import fr.sorbonne_u.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.sorbonne_u.datacenter.software.applicationvm.ApplicationVM;
import fr.sorbonne_u.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.sorbonne_u.datacenter.software.applicationvm.interfaces.ApplicationVMManagementI;
import fr.sorbonne_u.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.sorbonne_u.datacenter.software.requestdispatcher.connectors.RequestDispatcherManagementConnector;
import fr.sorbonne_u.datacenter.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.sorbonne_u.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementInboundPort;
import fr.sorbonne_u.datacenter.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;
import fr.sorbonne_u.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.sorbonne_u.datacenterclient.requestgenerator.interfaces.RequestGeneratorManagementI;
import fr.sorbonne_u.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;

/**
 * The class <code>Integrator</code> plays the role of an overall supervisor
 * for the data center example.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : 2018-09-21</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				Integrator
extends		AbstractComponent
{
	protected String									rmipURI ;
	protected String									csipURI ;
	protected String									rdipURI ;
	protected RequestDispatcherManagementOutboundPort	rdop ;
	/** Port connected to the request generator component to manage its
	 *  execution (starting and stopping the request generation).			*/
	protected RequestGeneratorManagementOutboundPort	rmop ;
	/** Port connected to the computer component to access its services.	*/
	protected ComputerServicesOutboundPort			csop ;
	/** Port connected to the AVM component to allocate it cores.			*/
	//	protected ApplicationVMManagementOutboundPort	avmop ;

	protected Map<ApplicationVM,ApplicationVMManagementOutboundPort> mapVMPorts;

	protected String									avmipURI ;
	protected final int NUMBER_OF_CORES=4;

	public				Integrator(
			String csipURI,
			Vector<ApplicationVM> applicationsVMs,
			String rdipURI,
			String rmipURI
			) throws Exception
	{
		super(1, 0) ;


		assert	csipURI != null && avmipURI != null && rmipURI != null && rdipURI != null ;
		this.rmipURI = rmipURI ;
		this.rdipURI = rdipURI ;
		//	this.avmipURI = avmipURI ;
		this.avmipURI = applicationsVMs.get(0).getApplicationVMManagementInboundPortURI() ;
		this.csipURI = csipURI ;

		this.addRequiredInterface(ComputerServicesI.class) ;
		this.addRequiredInterface(RequestGeneratorManagementI.class) ;
		this.addRequiredInterface(RequestDispatcherManagementI.class) ;
		this.addRequiredInterface(ApplicationVMManagementI.class) ;

		this.csop = new ComputerServicesOutboundPort(this) ;
		this.addPort(this.csop) ;
		this.csop.publishPort() ;

		this.rmop = new RequestGeneratorManagementOutboundPort(this) ;
		this.addPort(rmop) ;
		this.rmop.publishPort() ;

		this.rdop = new RequestDispatcherManagementOutboundPort(this) ;
		this.addPort(this.rdop) ;
		this.rdop.publishPort() ;


		this.mapVMPorts = new HashMap<ApplicationVM,ApplicationVMManagementOutboundPort>();
		ApplicationVMManagementOutboundPort avmop;
		for(ApplicationVM vm : applicationsVMs)
		{
			avmop = new ApplicationVMManagementOutboundPort(this) ;
			this.addPort(avmop) ;
			avmop.publishPort() ;			
			mapVMPorts.put(vm, avmop);
		}
		

	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#start()
	 */
	@Override
	public void			start() throws ComponentStartException
	{
		super.start() ;

		try {
			this.doPortConnection(
					this.csop.getPortURI(),
					this.csipURI,
					ComputerServicesConnector.class.getCanonicalName()) ;
			this.doPortConnection(
					this.rmop.getPortURI(),
					rmipURI,
					RequestGeneratorManagementConnector.class.getCanonicalName()) ;	
			this.doPortConnection(
					this.rdop.getPortURI(),
					rdipURI,
					RequestDispatcherManagementConnector.class.getCanonicalName()) ;		


			for(ApplicationVM vm:this.mapVMPorts.keySet())
			{

				this.doPortConnection(
						this.mapVMPorts.get(vm).getPortURI(),
						vm.getApplicationVMManagementInboundPortURI(),
						ApplicationVMManagementConnector.class.getCanonicalName()) ;

			}


		} catch (Exception e) {
			throw new ComponentStartException(e) ;
		}
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#execute()
	 */
	@Override
	public void			execute() throws Exception
	{
		super.execute() ;
		for(ApplicationVM vm:this.mapVMPorts.keySet())
		{
			//on fait une répartition des coeurs qu'on a crees pour toutes les aVM - voir l. 159 TestRequestGenerator 
			//computer0. Après avec la création du controlleur d'admission la répartition des coeurs se fera 
			//automatiquement
			AllocatedCore[] ac = this.csop.allocateCores(NUMBER_OF_CORES) ;
			
			vm.setAllocatedCores(ac);
			this.mapVMPorts.get(vm).allocateCores(ac) ;			
		}
//		for(ApplicationVMManagementOutboundPort avmop:this.mapVMPorts.values())
//		{
//			AllocatedCore[] ac = this.csop.allocateCores(NUMBER_OF_CORES) ;
//
//			avmop.allocateCores(ac) ;			
//		}

		/*Q1 : Aprés la création du controleur d'admission est ce qu'on aura toujours besoin de l'Integrator
					--> si oui
					Est ce qu'on va créer un Integrator par VM ou  on aura un seul integrator pour toutes les VMs
					--> si non
					ça y'est on va ecrire le code de l'integrator autrement
		 */

		this.rmop.startGeneration() ;
		// wait 2 seconds
		Thread.sleep(5000L) ;
		//Thread.sleep(10000L) ;
		
		// then stop the generation.
		this.rmop.stopGeneration() ;
		
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#finalise()
	 */
	@Override
	public void			finalise() throws Exception
	{
		this.doPortDisconnection(this.csop.getPortURI()) ;
		/*this.doPortDisconnection(this.avmop.getPortURI()) ;*/
		this.doPortDisconnection(this.rdop.getPortURI()) ;
		this.doPortDisconnection(this.rmop.getPortURI()) ;
		for(ApplicationVMManagementOutboundPort avmop:this.mapVMPorts.values())
			this.doPortDisconnection(avmop.getPortURI()) ;
		super.finalise();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdown()
	 */
	@Override
	public void			shutdown() throws ComponentShutdownException
	{
		try {
			this.csop.unpublishPort() ;
			/*	this.avmop.unpublishPort() ;*/
			this.rdop.unpublishPort() ;
			this.rmop.unpublishPort() ;
			for(ApplicationVMManagementOutboundPort avmop:this.mapVMPorts.values())
				avmop.unpublishPort() ;

		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdown();
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractComponent#shutdownNow()
	 */
	@Override
	public void			shutdownNow() throws ComponentShutdownException
	{
		try {
			this.csop.unpublishPort() ;
			/*			this.avmop.unpublishPort() ;*/
			this.rdop.unpublishPort() ;
			this.rmop.unpublishPort() ;
			for(ApplicationVMManagementOutboundPort avmop:this.mapVMPorts.values())
				avmop.unpublishPort() ;
		} catch (Exception e) {
			throw new ComponentShutdownException(e) ;
		}
		super.shutdownNow();
	}
}
