package com.plectix.simulator;

import java.util.ArrayList;

import org.apache.commons.cli.ParseException;

import com.plectix.simulator.simulator.SimulatorCommandLine;
import com.plectix.simulator.simulator.options.SimulatorFlagOption;
import com.plectix.simulator.simulator.options.SimulatorOption;
import com.plectix.simulator.simulator.options.SimulatorParameterizedOption;
import com.plectix.simulator.util.CommandLineUtils;

public final class SimulatorTestOptions {
	private final ArrayList<String> arguments = new ArrayList<String>();
	
	public final SimulatorCommandLine toCommandLine() throws ParseException {
		String[] argumentsArray = arguments.toArray(new String[arguments.size()]);
		arguments.clear();
		return this.createCommandLine(argumentsArray);
	}
	
	public static final SimulatorCommandLine defaultContactMapCommandLine(String filename, Integer opMode) 
				throws ParseException {
		return defaultContactMapCommandLine(filename, false, opMode);
	}
	
	public static final SimulatorCommandLine defaultContactMapCommandLine(String filename, boolean influenceMap, Integer opMode) 
				throws ParseException {
		return defaultContactMapOptions(filename, influenceMap, opMode).toCommandLine();
	}
	
	public static final SimulatorTestOptions defaultContactMapOptions(String filename, Integer opMode) {
		return defaultContactMapOptions(filename, false, opMode);
	}
	
	public static final SimulatorTestOptions defaultContactMapOptions(String filename, boolean influenceMap, Integer opMode) {
		SimulatorTestOptions options = new SimulatorTestOptions();
		options.append(SimulatorFlagOption.SHORT_CONSOLE_OUTPUT);
		options.appendContactMap(filename);
		options.append(SimulatorFlagOption.NO_DUMP_ITERATION_NUMBER);
		options.append(SimulatorFlagOption.NO_DUMP_RULE_ITERATION);
		if (influenceMap) {
			options.append(SimulatorFlagOption.BUILD_INFLUENCE_MAP);
		} else {
			options.append(SimulatorFlagOption.NO_BUILD_INFLUENCE_MAP);			
		}
		options.append(SimulatorFlagOption.NO_COMPUTE_QUALITATIVE_COMPRESSION);
		options.append(SimulatorFlagOption.NO_COMPUTE_QUANTITATIVE_COMPRESSION);
		options.append(SimulatorFlagOption.NO_ENUMERATE_COMPLEXES);
//		options.append(SimulatorOption.ALLOW_INCOMPLETE_SUBSTANCE);
		
		options.appendOperationMode(opMode);
		return options;
	}

	public final void appendOperationMode(Integer opMode) {
		
		append(SimulatorParameterizedOption.OPERATION_MODE);
		if (opMode!= null)
			append(opMode.toString());
		else 
			append("1");
	}
	
	public final void appendContactMap(String filename) {
		append(SimulatorParameterizedOption.CONTACT_MAP);
		append(filename);
	}
	
	public final void appendFocus(String filename) {
		append(SimulatorParameterizedOption.FOCUS_ON);
		append(filename);
	}
	
	public final void append(SimulatorOption option) {
		arguments.add("--" + option.getLongName());
	}
	
	private final void append(String option) {
		arguments.add(option);
	}
	
	private final SimulatorCommandLine createCommandLine(String[] args) throws ParseException {
		return new SimulatorCommandLine(CommandLineUtils.normalize(args));
	}

	public void appendSimulation(String filename) {
		append(SimulatorParameterizedOption.SIMULATIONFILE);
		append(filename);
	}

	public void appendQuantitativeCompression(String filename) {
		append(SimulatorFlagOption.OUTPUT_QUANTITATIVE_COMPRESSION);
		append(filename);
	}
	
}
