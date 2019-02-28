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
