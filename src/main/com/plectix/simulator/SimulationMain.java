package com.plectix.simulator;

import java.io.PrintStream;
import java.util.Locale;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.PropertyConfigurator;

import com.plectix.simulator.controller.SimulationService;
import com.plectix.simulator.controller.SimulatorCallable;
import com.plectix.simulator.controller.SimulatorCallableListener;
import com.plectix.simulator.controller.SimulatorInputData;
import com.plectix.simulator.simulator.DefaultSimulatorFactory;
import com.plectix.simulator.simulator.SimulatorCommandLine;
import com.plectix.simulator.simulator.ThreadLocalData;
import com.plectix.simulator.util.io.PlxLogger;

public class SimulationMain  {
	private static final String LOG4J_PROPERTIES_FILENAME = "config/log4j.properties";

	private static boolean loggingInitialized = false;
	
	private static final PlxLogger LOGGER = ThreadLocalData.getLogger(SimulationMain.class);

	private static final PrintStream DEFAULT_OUTPUT_STREAM = System.err;
	
	public static final String COMMAND_LINE_SYNTAX = "use --sim [file] [options]";
	
	public static void main(String[] args) {
		initializeLogging();
	
		SimulationService service = new SimulationService(new DefaultSimulatorFactory());
		service.submit(getSimulatorInputData(args, DEFAULT_OUTPUT_STREAM), new SimulatorCallableListener() {		
			@Override
			public void finished(SimulatorCallable simulatorCallable) {
				Exception exception = simulatorCallable.getSimulatorExitReport().getException();
				if (exception == null) {
					System.exit(0);
				} else {
					LOGGER.error("Simulator exited with an Exception", exception);
					System.exit(-1);
				}
			}
		});
		
		service.shutdown();
	}
	
	public static final SimulatorInputData getSimulatorInputData(String[] args, PrintStream printStream) {
		SimulatorCommandLine commandLine = null;
		try {
			commandLine = new SimulatorCommandLine(args);
		} catch (ParseException parseException) {
			parseException.printStackTrace();
			LOGGER.fatal("Caught fatal ParseException", parseException);
			System.exit(-2);
		}
		return new SimulatorInputData(commandLine.getSimulationArguments(), printStream);
	}

	public static final void initializeLogging() {
		if (loggingInitialized) {
			return;
		}
		
		// Initialize log4j
		PropertyConfigurator.configure(LOG4J_PROPERTIES_FILENAME);
		
		// Dump important info:
		LOGGER.info("Build Date: " + BuildConstants.BUILD_DATE);
		LOGGER.info("Revision: " + BuildConstants.BUILD_SVN_REVISION);
		LOGGER.info("Build OS: " + BuildConstants.BUILD_OS_NAME);
		LOGGER.info("Build Java Version: " + BuildConstants.JAVA_VERSION);
		LOGGER.info("Ant Java Version: " + BuildConstants.ANT_JAVA_VERSION);

		LOGGER.info("OS: " 
				+ System.getProperties().get("os.name") + " "
				+ System.getProperties().get("os.version") + ", "
				+ System.getProperties().get("os.arch"));
		
		LOGGER.info("Java Version: " 
				+ System.getProperties().get("java.version") + ", "
				+ System.getProperties().get("java.vendor"));
		
		LOGGER.info("Java Runtime: " 
				+ System.getProperties().get("java.runtime.name") + ", "
				+ System.getProperties().get("java.runtime.version"));
		
		LOGGER.info("Java VM: " 
				+ System.getProperties().get("java.vm.name") + ", "
				+ System.getProperties().get("java.vm.version") + ", "
				+ System.getProperties().get("java.vm.vendor") + ", "
				+ System.getProperties().get("java.vm.info"));
		
		LOGGER.info("Java Specifications: " +
				System.getProperties().get("java.specification.name") + ", "
				+ System.getProperties().get("java.specification.version") + ", "
				+ System.getProperties().get("java.specification.vendor"));
		
		LOGGER.info("Timezone: " + System.getProperties().get("user.timezone"));
		
		LOGGER.info("Default Locale: " + Locale.getDefault());
		
		loggingInitialized = true;
	}

}
