/*********************************************************************
 * Copyright (c)  2019 Expleo Germany GmbH [and others].
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Expleo Germany GmbH
 **********************************************************************/

package org.eclipse.kuksa.testing.model;

import java.util.List;

public class TestCase {
	private List<ResponseResult> results;
	private List<TestData> testData;

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

	/**
	 * @return all testData
	 */
	public List<TestData> getTestData() {
		return testData;
	}

	/**
	 * set the testData
	 * @param testData
	 */
	public void setTestData(List<TestData> testData) {
		this.testData = testData;
	}

	/**
	 *
	 * @param index to get the testData
	 * @return
	 */
	public TestData getTestData(int index) { return hasTestData(index) ? testData.get(index) : null; }

	public boolean hasTestData(int index) {
		return testData != null && testData.size() != 0 && index < testData.size() && index >= 0;

	}

}
