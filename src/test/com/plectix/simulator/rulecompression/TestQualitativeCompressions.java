package com.plectix.simulator.rulecompression;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.plectix.simulator.FileNameCollectionGenerator;
import com.plectix.simulator.RunAllTests;
import com.plectix.simulator.component.Rule;
import com.plectix.simulator.component.complex.localviews.LocalViewsMain;
import com.plectix.simulator.component.complex.subviews.AllSubViewsOfAllAgentsInterface;
import com.plectix.simulator.util.DefaultPropertiesForTest;

@RunWith(value = Parameterized.class)
public class TestQualitativeCompressions {
	private static final String prefixSourseModel = InitData.pathForSourseModel
			+ "qualitative" + File.separator;

	private LocalViewsMain localViews;
	private List<Rule> rules;

	private InitTestRuleCompressions initTestRuleCompressions = new InitTestRuleCompressions();
	
	@Parameters
	public static Collection<Object[]> configs() {
		return FileNameCollectionGenerator
		.getAllFileNamesWithPathWithModifyName(prefixSourseModel,
				"~kappa");
	}

	public TestQualitativeCompressions(String count, String patch) {
		initTestRuleCompressions.initializeSimulation(patch, count);
	}

	@Before
	public void setUp() {
		initLocalViews(initTestRuleCompressions.getSubViews());
		this.rules = initTestRuleCompressions.getRules();
	}

	private void initLocalViews(AllSubViewsOfAllAgentsInterface subViews) {

		if (localViews == null) {
			localViews = new LocalViewsMain(subViews);
			localViews.buildLocalViews();
		}
	}

	@Test
	public void testCompress() {
		QualitativeCompressor q = new QualitativeCompressor(localViews);
		q.buildGroups(rules);
		q.setLocalViews();
		q.compressGroups();
		System.out.println("rules");
		for (Rule r : rules) {
			System.out.println("=========================");
			System.out.println(r);
			System.out.println(q.getCompressedRule(r));
		}
		System.out.println("groups");
		LinkedHashSet<RootedRulesGroup> groupses = new LinkedHashSet<RootedRulesGroup>();
		groupses.addAll(q.getGroups());
		for (RootedRulesGroup rg : groupses) {
			System.out.println("++++++++++++++++++++++");
			System.out.println(rg.getCompressedRule());
			System.out.println("++++++++++++++++++++++");
		}

	}

}
