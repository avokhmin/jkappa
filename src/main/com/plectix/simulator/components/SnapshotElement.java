package com.plectix.simulator.components;

import com.plectix.simulator.components.solution.SuperSubstance;
import com.plectix.simulator.components.string.ConnectedComponentToSmilesString;
import com.plectix.simulator.interfaces.IConnectedComponent;

/**
 * This class implements snapshot element entity.
 * @author avokhmin
 *
 */
public final class SnapshotElement {
	private long count;
	private IConnectedComponent component;
	private final String ccName;

	/**
	 * Constructor. Creates snapshot-element with existing connected component
	 * @param connectedComponent given ConnectedComponent
	 * @param isOcamlStyleObsName <tt>true</tt> if we use O'caml styled observables names, 
	 * otherwise <tt>false</tt>
	 */
	public SnapshotElement(IConnectedComponent connectedComponent, long count, boolean isOcamlStyleObsName) {
		this.count = count;
		component = connectedComponent;
//		component.initSpanningTreeMap();
		ccName = component.getHash();
		//SimulationUtils.printPartRule(component, new int[] {0}, isOcamlStyleObsName);
	}
	
	/**
	 * This method returns counter of this snapshot element
	 * @return counter of this snapshot element
	 */
	public final long getCount() {
		return count;
	}

	/**
	 * This method returns name of current snapshot element
	 * @return name of current snapshot element
	 */
	public final String getComponentsName() {
		return ccName;
	}

	/**
	 * This method compares current connected component with the given one and
	 * increments counter for this snapshot element, if they are Automorphic's  
	 * @param connectedComponent given connected component
	 * @return <tt>true</tt> if given connected component is automorphic to current,  
	 * otherwise <tt>false</tt>
	 */
	public final boolean exists(IConnectedComponent connectedComponent) {
		if (component == connectedComponent)
			return true;
//		connectedComponent.initSpanningTreeMap();
		//if (cc.isAutomorphism(ccEx.getAgents().get(0))) {
//		if (component.unify(connectedComponent.getAgents().get(0)) 
//				&& connectedComponent.unify(component.getAgents().get(0))) {
		if(ccName.equals(connectedComponent.getHash())){
			SuperSubstance substance = connectedComponent.getSubstance();
			if (substance != null) {
				count += substance.getQuantity();
			} else {
				count++;
			}
			return true;
		}

		return false;
	}

	/**
	 * This method sets connected component of this snapshot element.
	 * @param component new values of connected component.
	 */
	public final void eraseConnectedComponent() {
		component = null;
	}
}
