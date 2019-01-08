package org.eclipse.kuksa.testing.model;

import java.util.List;

public class TestCase {

	private List<ResponseResult> results;

	/**
	 * @return the results
	 */
	public List<ResponseResult> getResults() {
		return results;
	}

	/**
	 * @param results the results to set
	 */
	public void setResults(List<ResponseResult> results) {
		this.results = results;
	}

	public ResponseResult getResult(int index) {
		return hasResult(index) ? results.get(index) : null;
	}

	private boolean hasResult(int index) {
		return results != null && results.size() != 0 && index < results.size() && index >= 0;
	}

	private List<TestData> testData;

	public TestData getTestData(int index) { return hasTestData(index) ? testData.get(index) : null; }

	public boolean hasTestData(int index) {
		return testData != null && testData.size() != 0 && index < testData.size() && index >= 0;

	}

}
