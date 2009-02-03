package com.plectix.simulator.simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.CharBuffer;

import com.plectix.simulator.util.PersistenceUtils;

public class SimulationArguments {

	public static final int NUMBER_OF_MILLISECONDS_IN_SECOND = 1000;
	public static final int NUMBER_OF_MILLISECONDS_IN_MINUTE = 60 * NUMBER_OF_MILLISECONDS_IN_SECOND;
	public static final int NUMBER_OF_MILLISECONDS_IN_HOUR = 60 * NUMBER_OF_MILLISECONDS_IN_MINUTE;
	public static final int NUMBER_OF_MILLISECONDS_IN_DAY = 24 * NUMBER_OF_MILLISECONDS_IN_HOUR;
	
	public static final int DEFAULT_SEED = -1;
	public static final long DEFAULT_MAX_CLASHES = 100;
	public static final int DEFAULT_NUMBER_OF_POINTS = 1000;
	public static final long DEFAULT_WALL_CLOCK_TIME_LIMIT = NUMBER_OF_MILLISECONDS_IN_DAY;
	public static final String DEFAULT_XML_SESSION_NAME = "simplx.xml";
	public static final String DEFAULT_SERIALIZATION_FILE_NAME = "~tmp.sd";

	public enum SimulationType { 
		NONE,
		COMPILE,
		STORIFY,
		SIM,
		AVERAGE_OF_RUNS,
		GENERATE_MAP,
		CONTACT_MAP
	}

	public enum StorifyMode {
		/** Sets the mode for stories to No Compression */
		NONE,
		/** Sets the mode for stories to Weak Compression */
		WEAK,
		/** Sets the mode for stories to Strong Compression */
		STRONG
	}

	public enum SerializationMode {
		NONE,
		READ,
		SAVE
	}
	
	private boolean noDumpStdoutStderr = false;
	private boolean help = false;
	private boolean version = false;
	private boolean shortConsoleOutput = false;
	private String xmlSessionName = DEFAULT_XML_SESSION_NAME;
	private double initialTime = 0.0;
	private int points = -1;
	private double rescale = Double.NaN;
	private int seed = DEFAULT_SEED;
	private long maxClashes = DEFAULT_MAX_CLASHES;
	private long event;
	private double timeLength = 0;
	private boolean isTime = false;
	private int iterations = 1;
	private String randomizer = null;
	private boolean activationMap = true;
	private boolean inhibitionMap = false;
	private boolean compile = false;
	private boolean debugInit = false;
	private boolean genereteMap = false;
	private boolean contactMap = false;
	private boolean numberOfRuns = false;
	private boolean storify = false;
	private boolean forwardOnly = false;
	private boolean ocamlStyleObservableNames = false;
	private long wallClockTimeLimit = DEFAULT_WALL_CLOCK_TIME_LIMIT;
	private boolean outputFinalState = false;
	private String xmlSessionPath = "";
	private String serializationFileName = DEFAULT_SERIALIZATION_FILE_NAME;
	private String inputFile = null;
	private String inputFilename = null;	
	private String snapshotsTimeString = null;
	private String focusFilename = null;
	private String commandLineString = null;
	private SimulationType simulationType = SimulationType.NONE;
	private StorifyMode storifyMode = StorifyMode.NONE;
	private SerializationMode serializationMode = SerializationMode.NONE;
	
	
	public SimulationArguments() {
		super();
	}
	
	public static void main(String[] args) throws Exception {
		SimulationArguments simulationArguments = new SimulationArguments();
		PersistenceUtils.addAlias(simulationArguments);
		
		System.err.println("==================== DEFAULT SIMULATION ARGUMENTS ====================");
		System.err.println(PersistenceUtils.getXStream().toXML(simulationArguments));
		
		System.err.println("==================== SIMULATION ARGUMENTS EXAMPLE 1 ====================");
		SimulatorCommandLine commandLine = new SimulatorCommandLine(new String[]{"--sim", "file.ka", "--time", "100"});
		simulationArguments = commandLine.getSimulationArguments();
		System.err.println(PersistenceUtils.getXStream().toXML(simulationArguments));

		System.err.println("==================== SIMULATION ARGUMENTS EXAMPLE 2 ====================");
		commandLine = new SimulatorCommandLine(new String[]{"--sim", "file.ka", "--event", "1000"});
		simulationArguments = commandLine.getSimulationArguments();
		System.err.println(PersistenceUtils.getXStream().toXML(simulationArguments));
		
		System.err.println("==================== SIMULATION ARGUMENTS EXAMPLE 3 ====================");
		commandLine = new SimulatorCommandLine(new String[]{"--storify", "file.ka", "--iteration", "50"});
		simulationArguments = commandLine.getSimulationArguments();
		System.err.println(PersistenceUtils.getXStream().toXML(simulationArguments));
		
		System.err.println("==================== SIMULATION ARGUMENTS EXAMPLE 4 ====================");
		commandLine = new SimulatorCommandLine(new String[]{"--help"});
		simulationArguments = commandLine.getSimulationArguments();
		System.err.println(PersistenceUtils.getXStream().toXML(simulationArguments));
		
		System.err.println("==================== SIMULATION ARGUMENTS EXAMPLE 5 ====================");
		commandLine = new SimulatorCommandLine(new String[]{"--sim", "file.ka", "--time", "100"});
		simulationArguments = commandLine.getSimulationArguments();
		File file = new File("data/Example.ka");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		CharBuffer buffer = CharBuffer.allocate((int) file.length());
		reader.read(buffer);
		String inputFile = buffer.rewind().toString();
		simulationArguments.setInputFile(inputFile);
		System.err.println(PersistenceUtils.getXStream().toXML(simulationArguments));

	}
	
	
	//**************************************************************************
	//
	// GETTERS AND SETTERS
	// 
	//
	
	public final String getXmlSessionName() {
		return xmlSessionName;
	}
	
	/**
	 * Sets the XML file name where the output is saved.
	 * <br><br>
	 * Corresponds to "--xml-session-name" option in simplx. Default value is {@value #DEFAULT_XML_SESSION_NAME}.
	 * 
	 * @param xmlSessionName
	 * @see #DEFAULT_XML_SESSION_NAME
	 */
	public final void setXmlSessionName(String xmlSessionName) {
		this.xmlSessionName = xmlSessionName;
	}
	
	public final double getInitialTime() {
		return initialTime;
	}
	
	/**
	 * Sets the parameter to start taking measures (stories) at indicated time.
	 * <br><br>
	 * Corresponds to "--init" option in simplx. Default value is 0.0.
	 * 
	 * @param initialTime
	 */
	public final void setInitialTime(double initialTime) {
		this.initialTime = initialTime;
	}
	
	public final int getPoints() {
		return points;
	}
	
	/**
	 * Corresponds to "--points" option in simplx. Default value is {@value #DEFAULT_NUMBER_OF_POINTS}.
	 * 
	 * @param points
	 * @see #DEFAULT_NUMBER_OF_POINTS
	 */
	public final void setPoints(int points) {
		this.points = points;
	}
	
	public final double getRescale() {
		return rescale;
	}

	/**
	 * Sets the rescaling factor for the initial condition.
	 * The counts for all species are multiplied by the given factor when creating the initial condition.
	 * <br><br>
	 * Corresponds to "--rescale" option in simplx. 
	 * 
	 * @param rescale
	 */
	public final void setRescale(double rescale) {
		this.rescale = rescale;
	}
	
	public final int getSeed() {
		return seed;
	}
	
	/**
	 * Sets the seed for the random number generator.
	 * Same integer will generate the same random number sequence, 
	 * except when set to {@link #DEFAULT_SEED} which sets a random seed for each simulation. 
	 * <br><br>
	 * Corresponds to "--seed" option in simplx. 
	 * Default value is {@value #DEFAULT_SEED} which sets a random seed each time.
	 * 
	 * @param seed
	 * @see #DEFAULT_SEED
	 */
	public final void setSeed(int seed) {
		this.seed = seed;
	}
	
	public final long getMaxClashes() {
		return maxClashes;
	}

	/**
	 * Sets the maximum number of consecutive clashes before aborting the simulation.
	 * <br><br>
	 * When we select a rule, we don't know whether its injections would "clash" with each other. 
	 * If it does so, then we select a new rule. and most of the time we apply it with no clash... 
	 * and simulation continues.
	 * <br><br>
	 * But sometimes we may have another clash, so we count the number of consecutive 
	 * clashes until we can apply a rule. if we have maxClashes consecutive clashes we assume 
	 * that we have a deadlock and we stop the simulation.
	 * <br><br>
	 * Corresponds to "--max-clashes" option in simplx. Default value is {@value #DEFAULT_MAX_CLASHES}.
	 * 
	 * @param maxClashes
	 * @see #DEFAULT_MAX_CLASHES
	 */
	public final void setMaxClashes(long maxClashes) {
		this.maxClashes = maxClashes;
	}

	public final boolean isTime() {
		return isTime;
	}
	
	/**
	 * Sets whether the simulation would run up to a certain time or to a certain number of events.
	 * If "--time" option is specified on the command line, it is set to <code>true</code>. 
	 * If "--event" option is specified on the command line, it is set to <code>false</code>.
	 * 
	 * @param isTime
	 * @see #setTimeLength(double)
	 * @see #setEvent(long)
	 */
	public final void setTime(boolean isTime) {
		this.isTime = isTime;
	}
	
	public final double getTimeLength() {
		return timeLength;
	}
	
	/**
	 * Sets the time limit until when the simulation would run.
	 * This parameter is discarded if the simulation is event-based.
	 * <br><br>
	 * Corresponds to "--time" option in simplx.
	 * 
	 * @param timeLength
	 * @see #isTime()
	 */
	public final void setTimeLength(double timeLength) {
		this.timeLength = timeLength;
	}

	public final long getEvent() {
		return event;
	}

	/**
	 * Sets the number of events (i.e. rule application) the simulation would run for.
	 * This parameter is discarded if the simulation is time-based.
	 * <br><br>
	 * Corresponds to "--event" option in simplx. 
	 * 
	 * @param event
	 * @see #isTime()
	 */
	public final void setEvent(long event) {
		this.event = event;
	}

	
	public final String getRandomizer() {
		return randomizer;
	}
	
	/**
	 * Registers an optional executable to use as an external random number generator.
	 * <br><br>
	 * If set to <code>null</code>, 
	 * {@link <a href="http://java.sun.com/javase/6/docs/api/java/util/Random.html">java.util.Random</a>} 
	 * is used as the random number generator.
	 * Otherwise, the given randomizer is run in a new process through 
	 * <code>Runtime.getRuntime().exec(randomizer)</code> and its output is scanned to get random numbers.
	 * <br><br>
	 * The default value is <code>null</code>.
	 * 
	 * @param randomizer
	 */
	public final void setRandomizer(String randomizer) {
		this.randomizer = randomizer;
	}
	
	public final boolean isActivationMap() {
		return activationMap;
	}
	
	/**
	 * If set to <code>true</code>, the Activation Map (also known as Influence Map) is computed 
	 * and saved into the output XML file.
	 * 
	 * @param activationMap
	 */
	public final void setActivationMap(boolean activationMap) {
		this.activationMap = activationMap;
	}
	
	public final boolean isInhibitionMap() {
		return inhibitionMap;
	}
	
	/**
	 * If set to <code>true</code>, the Inhibition Map is computed 
	 * and saved into the output XML file.
	 * @param inhibitionMap
	 */
	public final void setInhibitionMap(boolean inhibitionMap) {
		this.inhibitionMap = inhibitionMap;
	}
	
	public final boolean isCompile() {
		return compile;
	}
	
	/**
	 * If set to <code>true</code>, rules, perturbations and the initial condition are compiled 
	 * and dumped to the output stream. This is needed for debugging purposes.
	 * <br><br>
	 * Corresponds to the "--compile" option of simplx.
	 * 
	 * @param compile
	 */
	public final void setCompile(boolean compile) {
		this.compile = compile;
	}
	
	public final boolean isDebugInit() {
		return debugInit;
	}
	
	/**
	 * 	Corresponds to the "--debug" option of simplx. 
	 *  Program exits right after the initialization phase. Used mainly for debugging.
	 * 	
	 * @param debugInitOption
	 */
	public final void setDebugInit(boolean debugInitOption) {
		this.debugInit = debugInitOption;
	}
	
	public final boolean isGenereteMap() {
		return genereteMap;
	}
	
	public final void setGenereteMap(boolean genereteMapOption) {
		this.genereteMap = genereteMapOption;
	}
	
	public final boolean isContactMap() {
		return contactMap;
	}
	
	public final void setContactMap(boolean contactMapOption) {
		this.contactMap = contactMapOption;
	}
	
	public final boolean isNumberOfRuns() {
		return numberOfRuns;
	}
	
	public final void setNumberOfRuns(boolean numberOfRunsOption) {
		this.numberOfRuns = numberOfRunsOption;
	}
	
	public final boolean isStorify() {
		return storify;
	}
	
	/**
	 * If set to <code>true</code>, the simulator runs in the Stories mode.
	 * 
	 * Corresponds to the "--storify" option of simplx.
	 * 
	 * @param storifyOption
	 */
	public final void setStorify(boolean storifyOption) {
		this.storify = storifyOption;
	}
	
	public final boolean isForwardOnly() {
		return forwardOnly;
	}
	
	/**
	 * If set to <code>true</code>, the simulator does not consider backward rules.
	 * <br><br>
	 * Corresponds to the "--forward" option of simplx.
	 * 
	 * @param forwardOption
	 */
	public final void setForwardOnly(boolean forwardOption) {
		this.forwardOnly = forwardOption;
	}
	
	public final boolean isOcamlStyleObservableNames() {
		return ocamlStyleObservableNames;
	}
	
	/**
	 * If <code>true</code>, the observable names match those of simplx.
	 * <br><br>
	 * Our OCaml simulator, simplx, doesn't keep the observable names specified in the Kappa file
	 * but rewrites them in the Plot Text argument of the XML output file. For example:
	 * <br><br>
	 * <code> %obs: A(r,l)</code>
	 * <br><br>
	 * in a Kappa file is plotted as <code>A(l,r)</code> by simplx.
	 * Under simplx, the internal representation of the sites is a Set and 
	 * simplx doesn't have access to the original strings once they are compiled.
	 * <br><br>
	 * Through this option, the original names mentioned in the Kappa file are converted to simplx-style 
	 * names when they are written to the output file. The conversion
	 * <br><br>
	 * <li> orders the site names alphabetically,
	 * <li> removes all unnecessary white spaces, and
	 * <li> relabels all link numbers starting from 0 at the left hand side and then incrementing it to the right.
	 * <br><br>
	 * The default option is to save the names as they appear in the Kappa file. 
	 * @param ocamlStyleObservableNames
	 */
	public final void setOcamlStyleObservableNames(boolean ocamlStyleObservableNames) {
		this.ocamlStyleObservableNames = ocamlStyleObservableNames;
	}
	
	public final long getWallClockTimeLimit() {
		return wallClockTimeLimit;
	}
	
	/**
	 * Registers a wall clock time limit in milliseconds. 
	 * The simulation is interrupted if it lasts more than this limit.
	 * The default value is {@value #DEFAULT_WALL_CLOCK_TIME_LIMIT}. 
	 * 
	 * @param wallClockTimeLimit
	 * @see #DEFAULT_WALL_CLOCK_TIME_LIMIT
	 */
	public final void setWallClockTimeLimit(long wallClockTimeLimit) {
		this.wallClockTimeLimit = wallClockTimeLimit;
	}
	
	public final boolean isOutputFinalState() {
		return outputFinalState;
	}
	
	/**
	 * Corresponds to the "--output_final_state" option of simplx. 
	 * Same as using --set-snapshot-time for the last time unit.
	 * 
	 * @param outputFinalState
	 */
	public final void setOutputFinalState(boolean outputFinalState) {
		this.outputFinalState = outputFinalState;
	}
	
	public final String getXmlSessionPath() {
		return xmlSessionPath;
	}
	
	/**
	 * Sets the current directory to save the output data.
	 * <br><br>
	 * Corresponds to "--output-scheme" option in simplx. 
	 * @param xmlSessionPath
	 */
	public final void setXmlSessionPath(String xmlSessionPath) {
		this.xmlSessionPath = xmlSessionPath;
	}
	
	public final String getSerializationFileName() {
		return serializationFileName;
	}
	
	/**
	 * Corresponds to the "--save_all" option of simplx which requires
	 * an argument as the name of the file in which to save the whole 
	 * initialization's marshalling (including influence maps).
	 * The default value is {@value #DEFAULT_SERIALIZATION_FILE_NAME}.
	 * 
	 * @param serializationFileName
	 * @see #DEFAULT_SERIALIZATION_FILE_NAME
	 */
	public final void setSerializationFileName(String serializationFileName) {
		this.serializationFileName = serializationFileName;
	}
	
	public final String getInputFilename() {
		return inputFilename;
	}
	
	public final void setInputFilename(String inputFile) {
		this.inputFilename = inputFile;
	}
	
	public final String getSnapshotsTimeString() {
		return snapshotsTimeString;
	}
	
	public final void setSnapshotsTimeString(String snapshotsTimeString) {
		this.snapshotsTimeString = snapshotsTimeString;
	}
	
	public final String getFocusFilename() {
		return focusFilename;
	}
	
	public final void setFocusFilename(String focusFilename) {
		this.focusFilename = focusFilename;
	}

	public final SimulationType getSimulationType() {
		return simulationType;
	}
	public final void setSimulationType(SimulationType simulationType) {
		this.simulationType = simulationType;
	}

	public final SerializationMode getSerializationMode() {
		return serializationMode;
	}

	public final void setSerializationMode(SerializationMode serializationMode) {
		this.serializationMode = serializationMode;
	}

	public final StorifyMode getStorifyMode() {
		return storifyMode;
	}

	public final void setStorifyMode(StorifyMode storifyMode) {
		this.storifyMode = storifyMode;
	}

	public final String getCommandLineString() {
		return commandLineString;
	}

	public final void setCommandLineString(String commandLineString) {
		this.commandLineString = commandLineString;
	}

	public final boolean isNoDumpStdoutStderr() {
		return noDumpStdoutStderr;
	}

	/**
	 * If <code>true</code>, nothing is dumped into either standard output or standard error.
	 * 
	 * @param noDumpStdoutStderr
	 */
	public final void setNoDumpStdoutStderr(boolean noDumpStdoutStderr) {
		this.noDumpStdoutStderr = noDumpStdoutStderr;
	}

	public final boolean isHelp() {
		return help;
	}

	/**
	 * If <code>true</code>, the usage information is dumped into the output stream.
	 * 
	 * @param help
	 */
	public final void setHelp(boolean help) {
		this.help = help;
	}

	public final boolean isVersion() {
		return version;
	}

	/**
	 * If <code>true</code>, the version number is dumped into the output stream.
	 * <br><br>
	 * Corresponds to the "--version" option of simplx.
	 * 
	 * @param version
	 */
	public final void setVersion(boolean version) {
		this.version = version;
	}

	public final String getInputFile() {
		return inputFile;
	}

	public final void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}
	
	public final int getIterations() {
		return iterations;
	}
	
	public final void setIterations(int iterations) {
		this.iterations = iterations;
	}
	
	public boolean isShortConsoleOutput() {
		return shortConsoleOutput;
	}

	/**
	 * If set to <code>true</code>, the console output is reduced.
	 * 
	 * @param shortConsoleOutput
	 */
	public void setShortConsoleOutput(boolean shortConsoleOutput) {
		this.shortConsoleOutput = shortConsoleOutput;
	}
}
