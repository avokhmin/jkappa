package KappaSimulationInterfaces;

import java.util.List;
import java.util.Map;

public interface IConnectedComponent {

	
	//TODO creating spanning tree and stuff
	public void precompile();
	
	public void precompilationToString();
	
	//TODO ???
	public Map<String, IConnectedComponent> unify( 
			ISolution solution, IAgent agent);
	public IInjection checkAndBuildInjection(ISolution solution, IAgent agent);
	
	public String getPrecompilationAsString();
	
	//TODO ???
	public List<IInjection> pushout();
	
	public List<IAgent> getAgents();
	
}
