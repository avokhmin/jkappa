package com.plectix.simulator.parser.abstractmodel.observables;

public abstract class ObservablesLineData  {
	private final int id;
	
	ObservablesLineData(int id) {
		this.id = id;
	}
	
	public final int getId() {
		return id;
	}
}
