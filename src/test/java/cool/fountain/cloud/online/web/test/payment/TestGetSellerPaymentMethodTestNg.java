package cool.fountain.cloud.online.web.test.payment;

import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import io.restassured.path.json.JsonPath;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("qa")
@AutoConfigureWebTestClient

public class TestGetSellerPaymentMethodTestNg extends AbstractTestNGSpringContextTests {
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
	@Parameters({ "apiKey", "apiUri","requestJsonfile", "sellerIdList", "paymentMethodList" })
	public void testGetPaymentMethod(String apiKey, String apiUri,String requestJsonfile, String sellerIdList, String paymentMethodList) {

		logger.info("Running GetSellerPaymentMethods");
		
		String url = getUrl(apiUri);
		
		WebTestClient testClient = WebTestClient.bindToServer().baseUrl(url).build();

		
		String json = TestAutomationUtil.getContent(requestJsonfile);
		
		RequestBodyUriSpec requestBody = testClient.post();
	
		
		addHeaders(requestBody, apiKey);
;
		
		BodyContentSpec body = requestBody.contentType(MediaType.APPLICATION_JSON).syncBody(json).exchange()
				.expectBody();
	
		String statusCode = body.returnResult().getStatus().toString();
		
		if (statusCode.equalsIgnoreCase("200 OK") ) {
			String responseString = new String(body.returnResult().getResponseBody());

			// Converting JsonObject into JsonPath
			JsonPath inputJson = new JsonPath(json);
			JsonPath outputJson = new JsonPath(responseString);
			
			// Checking for terminalId and sellerId	
			
			//if(checkSellerIdList.equals(responseSellerIdList))
			if(sellerIdList.equalsIgnoreCase(Integer.toString(outputJson.get("sellerPaymentOptions[0].sellerId")))
			{
				
				List<String> responsePaymentMethodsList = new ArrayList<String>();
				
				int moduleCount = outputJson.get("sellerPaymentOptions[0].paymentMethods.size()");
				
				for(int i=0; i<moduleCount; i++) {
					responsePaymentMethodsList.add(outputJson.get("sellerPaymentOptions[0].paymentMethods["+i+"].paymentMethodId").toString());
				}
				
				Collections.sort(responsePaymentMethodsList);
				
				List<String> checkPaymentMethodsList = new ArrayList<String>();
				
				checkPaymentMethodsList = Arrays.asList(paymentMethodList.split("\\s*,\\s*"));
				Collections.sort(checkPaymentMethodsList);
				
				if(checkPaymentMethodsList.equals(responsePaymentMethodsList)) {
					assertTrue(true);
				}
				else {
					assertTrue(false);
				}
			}
			else {
				logger.info("getSellerPaymentMethod is not returning same terminalId in the response");
				assertTrue(false);
			}
		}
		else {
			logger.info("getSellerPaymentMethod is returning statusCode "+statusCode+" insted of 200 ");
			assertTrue(false);
		}

		logger.info("Completed getSellerPaymentMethod");
	}

	@Test(invocationCount = 100, threadPoolSize = 10)
	@Parameters({ "apiKey", "apiUri", "requestJsonfile", "maxResponseTimeThreshold" })
	public void testGetPaymentMethodLoad(String apiKey, String apiUri, String requestJsonfile,
			Long maxResponseTimeThreshold)
			throws JsonParseException, JsonMappingException, IOException, JSONException, ParseException {
		logger.info("Running testGetTerminalConfigLoad");
		logger.info("responseTimeThreshold: " + maxResponseTimeThreshold);
		Long startTime = System.currentTimeMillis();
		getPaymentMethod(apiKey, apiUri, requestJsonfile);
		Long duration = System.currentTimeMillis() - startTime;
		assertTrue(duration <= maxResponseTimeThreshold,
				"Response time of GetTerminalConfig call more than: " + maxResponseTimeThreshold);
		logger.info("Completed testGetTerminalConfigLoad");
	}
	
	public void getPaymentMethod(String apiKey, String apiUri, String requestJsonfile) {
		String url = getUrl(apiUri);
		WebTestClient testClient = WebTestClient.bindToServer().baseUrl(url).build();
		String json = TestAutomationUtil.getContent(requestJsonfile);
		RequestBodyUriSpec requestBody = testClient.post();
		addHeaders(requestBody, apiKey);
		requestBody.contentType(MediaType.APPLICATION_JSON).syncBody(json).exchange()
				.expectStatus().isOk().expectBody();
	}
}
