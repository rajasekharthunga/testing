package cool.fountain.cloud.test.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TestAutomationUtil {
	private static Logger logger = LoggerFactory.getLogger(TestAutomationUtil.class);
	
	public static String getContent(String fileName) {
		String content = null;
		try {
			content = new String(Files.readAllBytes(Paths.get(fileName)));
		} catch (IOException e) {
			logger.error("Error printing from file: " + fileName, e);
		}
		return content;
	}
}
