package com.jl.hl.sinter.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

public class LoggerUtil {

	static {
		try {
			InputStream is = Resources.getResourceAsStream("log4j2.xml");
			ConfigurationSource source = new ConfigurationSource(is);
			Configurator.initialize(null, source);
		} catch (SecurityException e) {
			System.err.println(e);
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	public static Logger getLogger(String name) {
		Logger logger = LogManager.getLogger(name);
		return logger;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger logger = getLogger("Control");
		logger.info("I {} you {}", "love", "really?");
	}

}
