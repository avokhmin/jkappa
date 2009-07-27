package com.plectix.simulator.harness.contactMap;

import java.io.File;

import com.plectix.simulator.RunAllTests;

public class InitData {
	
	private static final String separator = File.separator;
	private static final String allPath = "test.data" + separator + "harness"+ separator + "contactMap" + separator;
	public static final String pathForSourseModel =  allPath + "model" + separator;
	public static final String pathForResult = allPath + "results" + separator;
	
	public static final String FILENAME_EXTENSION = RunAllTests.FILENAME_EXTENSION;
	
	public static int length = 4;

}
