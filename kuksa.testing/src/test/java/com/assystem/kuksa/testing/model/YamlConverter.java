package com.assystem.kuksa.testing.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.assystem.kuksa.testing.AbstractTestCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public final class YamlConverter {

	private static Logger logger = AbstractTestCase.LOGGER;

	// to avoid performance issues during mapping yaml-file to concrete class
	private static Map<String, Object> memory = new HashMap<>();

	/**
	 * Convert the file with the given path into a {@link TestSuite}.
	 * 
	 * @param filePath file path
	 * @return converted instance
	 */
	public static TestSuite convert(String filePath) {
		if (memory.containsKey(filePath)) {
			return (TestSuite) memory.get(filePath);
		}

		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		try {
			TestSuite result = mapper.readValue(new File(filePath), TestSuite.class);
			memory.put(filePath, result);
			return result;
		} catch (Exception e) {
			logger.error("Failed to convert yaml-file to obejct.", e);
		}

		return null;
	}

}
