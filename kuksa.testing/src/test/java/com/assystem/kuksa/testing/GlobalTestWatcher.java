package com.assystem.kuksa.testing;

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
		LOGGER.info("SKIPED TESTCASE " + description.getMethodName(), e);
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
