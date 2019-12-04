package cool.fountain.online.web.test.event.master;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec;
import org.springframework.test.web.reactive.server.WebTestClient.RequestBodyUriSpec;
import org.springframework.test.web.reactive.server.WebTestClient.RequestHeadersUriSpec;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import cool.fountain.cloud.test.util.TestAutomationUtil;
import cool.fountain.online.web.test.TestApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("qa")
@AutoConfigureWebTestClient

public class TestPostEventstestNg extends AbstractTestNGSpringContextTests{
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
	@Parameters({"apiKey", "apiUri","requestJsonfile"})
	public void testPostEvent(String apiKey,String apiUri, String requestJsonfile ) {
		
		logger.info("Running postEvents");

		String url = getUrl(apiUri);
		WebTestClient testClient = WebTestClient.bindToServer().baseUrl(url).build();
		String json = TestAutomationUtil.getContent(requestJsonfile);
		RequestBodyUriSpec requestBody = testClient.post();
		addHeaders(requestBody, apiKey);
		BodyContentSpec body = requestBody.contentType(MediaType.APPLICATION_JSON).syncBody(json).exchange()
				.expectStatus().isOk().expectBody();

		logger.info("Completed postEvents");
	}
	
	@Test(invocationCount = 100, threadPoolSize = 10)
	@Parameters({ "apiKey", "apiUri", "requestJsonfile", "maxResponseTimeThreshold" })
	public void testpostEventsLoad(String apiKey, String apiUri, String requestJsonfile, Long maxResponseTimeThreshold)
			throws JsonParseException, JsonMappingException, IOException, JSONException, ParseException {
		logger.info("Running testpostEventsLoad");
		logger.info("responseTimeThreshold: " + maxResponseTimeThreshold);
		Long startTime = System.currentTimeMillis();
		postEvents(apiKey, apiUri, requestJsonfile);
		Long duration = System.currentTimeMillis() - startTime;
		assertTrue(duration <= maxResponseTimeThreshold,
				"Response time of postEvents call more than: " + maxResponseTimeThreshold);
		logger.info("Completed testpostEventsLoad");
	}
	
	public void postEvents(String apiKey,String apiUri, String requestJsonfile ) {
		String url = getUrl(apiUri);
		WebTestClient testClient = WebTestClient.bindToServer().baseUrl(url).build();
		String json = TestAutomationUtil.getContent(requestJsonfile);
		RequestBodyUriSpec requestBody = testClient.post();
		addHeaders(requestBody, apiKey);
		BodyContentSpec body = requestBody.contentType(MediaType.APPLICATION_JSON).syncBody(json).exchange()
				.expectStatus().isOk().expectBody();
	}
}
