package in.shark.online.web.test.catalogue.master;

import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.json.JSONException;
import org.json.simple.parser.ParseException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import org.springframework.test.web.reactive.server.WebTestClient.RequestHeadersUriSpec;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import in.shark.online.web.test.TestApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("qa")
@AutoConfigureWebTestClient
public class TestGetAllProductsApiTestNg extends AbstractTestNGSpringContextTests{
	
	@Value("${server.url}")
	private String serverUrl;

	private Logger logger = LoggerFactory.getLogger(getClass());

	private void addHeaders(RequestHeadersUriSpec<?> requestBody, String apiKey) {
		requestBody.header("content-type", "application/json;charset=UTF-8");
		requestBody.header("service.auth.key", apiKey);
	}

	private String getUrl(String apiUri) {
		return serverUrl + apiUri;
	}
	@Test
	@Parameters({"apiKey", "apiUri","moduleId"})
	public void testGetAllProducts(String apiKey,String apiUri, String moduleId ) {
		
		logger.info("Running GetAllProducts");

		String url = getUrl(apiUri+"?moduleId="+moduleId);
		WebTestClient testClient = WebTestClient.bindToServer().baseUrl(url).build();
		RequestHeadersUriSpec<?> requestBody = testClient.get();
		addHeaders(requestBody, apiKey);
		BodyContentSpec body = requestBody.exchange().expectStatus().isOk().expectBody();

		logger.info("Completed GetAllProducts");
	}
	@Test(invocationCount = 100, threadPoolSize = 10)
	@Parameters({ "apiKey", "apiUri", "moduleId", "maxResponseTimeThreshold" })
	public void testGetAllProductstLoad(String apiKey, String apiUri, String moduleId, Long maxResponseTimeThreshold)
			throws JsonParseException, JsonMappingException, IOException, JSONException, ParseException {
		logger.info("Running testGetAllProductsLoad");
		logger.info("responseTimeThreshold: " + maxResponseTimeThreshold);
		Long startTime = System.currentTimeMillis();
		getAllProducts(apiKey, apiUri, moduleId);
		Long duration = System.currentTimeMillis() - startTime;
		assertTrue(duration <= maxResponseTimeThreshold,
				"Response time of GetAllProducts call more than: " + maxResponseTimeThreshold);
		logger.info("Completed testGetAllProductsLoad");
	}
	public void getAllProducts(String apiKey,String apiUri, String moduleId ) {
		String url = getUrl(apiUri+"?moduleId="+moduleId);
		WebTestClient testClient = WebTestClient.bindToServer().baseUrl(url).build();
		RequestHeadersUriSpec<?> requestBody = testClient.get();
		addHeaders(requestBody, apiKey);
		BodyContentSpec body = requestBody.exchange().expectStatus().isOk().expectBody();
	}

}
