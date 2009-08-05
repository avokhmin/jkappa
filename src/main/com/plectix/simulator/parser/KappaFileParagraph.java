package com.plectix.simulator.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class KappaFileParagraph {
	private Collection<KappaFileLine> myParagraph = new ArrayList<KappaFileLine>();
	
	public KappaFileParagraph() {
		
	}
	
	public void addLine(KappaFileLine line) { 
		myParagraph.add(line);
	}

	public boolean isEmpty() {
		return myParagraph.isEmpty();
	}
	
	public Collection<KappaFileLine> getLines() { 
		return myParagraph;
	}
}
