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

package org.eclipse.kuksa.testing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class GlobalTestWatcher extends TestWatcher {

	public static final Logger LOGGER = LogManager.getLogger();

	@Override
	protected void succeeded(Description description) {
		super.succeeded(description);
		LOGGER.info("TEST SUCCESSED");
	}

	@Override
	protected void failed(Throwable e, Description description) {
		super.failed(e, description);
		LOGGER.error("TEST FAILED", e);
	}

	@Override
	protected void skipped(AssumptionViolatedException e, Description description) {
		super.skipped(e, description);
		LOGGER.info("SKIPPED TESTCASE " + description.getMethodName(), e);
	}

	@Override
	protected void starting(Description description) {
		super.starting(description);
		LOGGER.info("--- START TESTCASE " + description.getMethodName() + " ---");
	}

	@Override
	protected void finished(Description description) {
		super.finished(description);
		LOGGER.info("--- FINISH TEST " + description.getMethodName() + " ---");
	}

}
