package org.eclipse.kuksa.testing.model;

import java.util.Map;

public class TestSuite {

	// mapping of test name and test result(s)
	private Map<String, TestCase> testCases;

	/**
	 * @return the testCases
	 */
	public Map<String, TestCase> getTestCases() {
		return testCases;
	}

	/**
	 * @param testCases the testCases to set
	 */
	public void setTestCases(Map<String, TestCase> testCases) {
		this.testCases = testCases;
	}

}
