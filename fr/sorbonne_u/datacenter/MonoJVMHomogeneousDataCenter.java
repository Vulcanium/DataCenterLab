package fr.sorbonne_u.datacenter;

// Copyright Jacques Malenfant, Sorbonne Universite.
// 
// Jacques.Malenfant@lip6.fr
// 
// This software is a computer program whose purpose is to provide a
// basic component programming model to program with components
// distributed applications in the Java programming language.
// 
// This software is governed by the CeCILL-C license under French law and
// abiding by the rules of distribution of free software.  You can use,
// modify and/ or redistribute the software under the terms of the
// CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
// URL "http://www.cecill.info".
// 
// As a counterpart to the access to the source code and  rights to copy,
// modify and redistribute granted by the license, users are provided only
// with a limited warranty  and the software's author,  the holder of the
// economic rights,  and the successive licensors  have only  limited
// liability. 
// 
// In this respect, the user's attention is drawn to the risks associated
// with loading,  using,  modifying and/or developing or reproducing the
// software by the user in light of its specific status of free software,
// that may mean  that it is complicated to manipulate,  and  that  also
// therefore means  that it is reserved for developers  and  experienced
// professionals having in-depth computer knowledge. Users are therefore
// encouraged to load and test the software's suitability as regards their
// requirements in conditions enabling the security of their systems and/or 
// data to be ensured and,  more generally, to use and operate it in the 
// same conditions as regards security. 
// 
// The fact that you are presently reading this means that you have had
// knowledge of the CeCILL-C license and that you accept its terms.

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.datacenter.hardware.computers.Computer;

/**
 * The class <code>MonoJVMDataCenter</code> defines the basis of a
 * simulated data center deployed as a component-based simulation
 * in a single JVM.
 *
 * <p><strong>Description</strong></p>
 * 
 * <p><strong>Invariant</strong></p>
 * 
 * <pre>
 * invariant		true
 * </pre>
 * 
 * <p>Created on : August 26, 2015</p>
 * 
 * @author	<a href="mailto:Jacques.Malenfant@lip6.fr">Jacques Malenfant</a>
 */
public class				MonoJVMHomogeneousDataCenter
extends		AbstractCVM
{
	// ------------------------------------------------------------------------
	// Constants and instance variables
	// ------------------------------------------------------------------------

	public static final int		NUMBER_OF_COMPUTERS = 10 ;
	public static final int		NUMBER_OF_PROCESSORS_PER_COMPUTER = 2 ;
	public static final int		NUMBER_OF_CORES_PER_PROCESSOR = 4 ;

	// Predefined URI of the different ports visible at the component assembly
	// level.
	public static final String	ComputerServicesInboundPortURIPrefix = "cs-ibp-" ;
	public static final String	ComputerServicesOutboundPortURIPrefix = "cs-obp" ;
	public static final String	ComputerStaticStateDataInboundPortURIPrefix = "css-dip-" ;
	public static final String	ComputerStaticStateDataOutboundPortURIPrefix = "css-dop-" ;
	public static final String	ComputerDynamicStateDataInboundPortURIPrefix = "cds-dip-" ;
	public static final String	ComputerDynamicStateDataOutboundPortURIPrefix = "cds-dop" ;

	/** default frequency of processors cores.							*/
	protected int					defautFrequency ;
	/** 	maximum difference in frequencies among cores of the same
	 *  processor.														*/
	protected int					maxFrequencyGap ;
	/** set of admissible frequencies for processors cores.				*/
	protected Set<Integer>			admissibleFrequencies ;
	/** 	map defining the relationship between the frequency of a core
	 *  and the number of instructions per second the core can process.	*/
	protected Map<Integer,Integer>	processingPower ;
	/** components simulating computers.									*/
	protected Computer[]				computers ;

	// ------------------------------------------------------------------------
	// Component virtual constructor
	// ------------------------------------------------------------------------

	/**
	 * create a set of computers in a simulated data center.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true			// no precondition.
	 * post	true			// no postcondition.
	 * </pre>
	 *
	 * @param admissibleFrequencies	set of admissible frequencies for processors cores.
	 * @param defautFrequency		default frequency of processors cores.
	 * @param maxFrequencyGap		maximum difference in frequencies among cores of the same processor.
	 * @param processingPower		map defining the relationship between the frequency of a core and the number of instructions per second the core can process.
	 * @throws Exception				<i>todo.</i>
	 */
	public				MonoJVMHomogeneousDataCenter(
		Set<Integer> admissibleFrequencies,
		int defautFrequency,
		int maxFrequencyGap,
		Map<Integer, Integer> processingPower
		) throws Exception
	{
		super();
		this.admissibleFrequencies = new HashSet<Integer>() ;
		for (Integer f : admissibleFrequencies) {
			this.admissibleFrequencies.add((Integer) f) ;
		}
		this.defautFrequency = defautFrequency ;
		this.maxFrequencyGap = maxFrequencyGap ;
		this.processingPower = new HashMap<Integer,Integer>() ;
		for (Integer fr : processingPower.keySet()) {
			this.processingPower.put(fr, processingPower.get(fr)) ;
		}

		this.computers = new Computer[NUMBER_OF_COMPUTERS] ;
	}

	// ------------------------------------------------------------------------
	// Component virtual machine methods
	// ------------------------------------------------------------------------

	/**
	 * @see fr.sorbonne_u.components.cvm.AbstractCVM#deploy()
	 */
	@Override
	public void			deploy() throws Exception
	{
		for(int c = 0 ; c < NUMBER_OF_COMPUTERS ; c++) {
			// ----------------------------------------------------------------
			// Create and deploy a computer component with its processors.
			// ----------------------------------------------------------------
			this.computers[c] = new Computer(
					"computer-" + c,
					this.admissibleFrequencies,
					this.processingPower,  
					this.defautFrequency,
					this.maxFrequencyGap,		// max frequency gap within a processor
					NUMBER_OF_PROCESSORS_PER_COMPUTER,
					NUMBER_OF_CORES_PER_PROCESSOR,
					ComputerServicesInboundPortURIPrefix + c,
					ComputerStaticStateDataInboundPortURIPrefix + c,
					ComputerDynamicStateDataInboundPortURIPrefix + c) ;
			this.addDeployedComponent(this.computers[c]) ;
		}

		// allow to complete the deployment in a subclass
		this.completeDeployment() ;
		// close the deployment at the component virtual machine level.
		super.deploy();

	}

	public void			completeDeployment()
	{
		// To be redefined in subclasses.
	}
}
