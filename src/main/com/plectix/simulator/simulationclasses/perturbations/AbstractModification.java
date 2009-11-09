package com.plectix.simulator.simulationclasses.perturbations;

public abstract class AbstractModification {
	private boolean performed = false;
	
	protected abstract void doItAll(); 
	
	public void perform() {
		this.doItAll();
		performed = true;
	}

	public boolean wasPerformed() {
		return performed;
	}

	// we may need this one in future
	public void reset() {
		performed = false;
	}
}
