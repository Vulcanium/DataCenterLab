package fr.sorbonne_u.datacenter.hardware.computers.ports;

import fr.sorbonne_u.components.AbstractComponent;

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

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.components.interfaces.DataRequiredI;
import fr.sorbonne_u.components.ports.AbstractDataOutboundPort;
import fr.sorbonne_u.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI;
import fr.sorbonne_u.datacenter.hardware.computers.interfaces.ComputerStaticStateDataI;
import fr.sorbonne_u.datacenter.hardware.computers.interfaces.ComputerStaticStateI;

/**
 * The class <code>ComputerStaticStateDataOutboundPort</code> implements a data
 * outbound port requiring the <code>ComputerStaticStateDataI</code> interface.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : April 14, 2015</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				ComputerStaticStateDataOutboundPort
extends		AbstractDataOutboundPort
implements	ComputerStaticStateDataI
{
	// ------------------------------------------------------------------------
	// Constants and instance variables
	// ------------------------------------------------------------------------

	private static final long	serialVersionUID = 1L;
	protected final String		computerURI ;
	
	//liste des URIS nécessaires
	

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public				ComputerStaticStateDataOutboundPort(
		ComponentI owner,
		String computerURI
		) throws Exception
	{
		super(DataRequiredI.PullI.class, DataRequiredI.PushI.class, owner) ;
		this.computerURI = computerURI ;

		assert	owner instanceof ComputerStateDataConsumerI ;
	}

	public				ComputerStaticStateDataOutboundPort(
		String uri,
		ComponentI owner,
		String computerURI
		) throws Exception
	{
		super(uri, DataRequiredI.PullI.class, DataRequiredI.PushI.class, owner);
		this.computerURI = computerURI ;

		assert	owner instanceof ComputerStateDataConsumerI ;
	}

	// ------------------------------------------------------------------------
	// Methods
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.interfaces.DataRequiredI.PushI#receive(fr.sorbonne_u.components.interfaces.DataRequiredI.DataI)
	 */
	@Override
	public void			receive(final DataRequiredI.DataI d) throws Exception
	{
		if (AbstractCVM.DEBUG_MODE.contains(CVMDebugModes.CALLING)) {
			System.out.println("ComputerStaticStateDataOutboundPort>>receive " +
									computerURI + " " + d) ;
		}

		this.owner.handleRequestAsync(
				new AbstractComponent.AbstractService<Void>() {
					@Override
					public Void call() throws Exception {
						((ComputerStateDataConsumerI)this.getOwner()).
							acceptComputerStaticData(
										computerURI,
										(ComputerStaticStateI)d) ;
						return null ;
					}
				}) ;
	}
}
