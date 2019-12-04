package cool.fountain.online.web.test.catalogue.master;

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
public class TestGetProductCatalogueApiTestNg extends AbstractTestNGSpringContextTests {
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
	@Parameters({ "apiKey", "apiUri", "requestJsonfile", "sellerIdList", "productListSize" })
	public void testGetProductCatalogue(String apiKey, String apiUri, String requestJsonfile, String sellerIdList,
			String productListSize) {

		logger.info("Running GetProductCatalogue");

		String url = getUrl(apiUri);
		WebTestClient testClient = WebTestClient.bindToServer().baseUrl(url).build();
		String json = TestAutomationUtil.getContent(requestJsonfile);
		RequestBodyUriSpec requestBody = testClient.post();
		addHeaders(requestBody, apiKey);
		BodyContentSpec body = requestBody.contentType(MediaType.APPLICATION_JSON).syncBody(json).exchange()
				.expectBody();

		String statusCode = body.returnResult().getStatus().toString();
		if (statusCode.equalsIgnoreCase("200 OK")) {
			String responseString = new String(body.returnResult().getResponseBody());
			// Converting JsonObject into JsonPath
			JsonPath outputJson = new JsonPath(responseString);

			List<String> checkProductList = new ArrayList<String>();
			checkProductList = Arrays.asList(productListSize.split("\\s*,\\s*"));
			List<String> responseSellerList = new ArrayList<String>();
			List<String> responseMenuIdList = new ArrayList<String>();
			int sellerCount = outputJson.get("sellerProductCatalogues.size()");
			for (int i = 0; i < sellerCount; i++) {
				responseSellerList.add(outputJson.get("sellerProductCatalogues[" + i + "].sellerId").toString());
//				responseMenuIdList.add(outputJson.get("sellerProductCatalogues[" + i + "].menuId").toString());
				int numberOfProducts = outputJson.get("sellerProductCatalogues[" + i + "].productCatalogue.products.size()");
				if(Integer.parseInt(checkProductList.get(i))!=numberOfProducts) {
					assertTrue(false);
				}
			}
			Collections.sort(responseSellerList);
			List<String> checkSellerList = new ArrayList<String>();
			checkSellerList = Arrays.asList(sellerIdList.split("\\s*,\\s*"));
			Collections.sort(checkSellerList);
			if (checkSellerList.equals(responseSellerList)) {
				assertTrue(true);
			} else {
				assertTrue(false);
			}

		} else {
			assertTrue(false);
		}

		logger.info("Completed GetProductCatalogue");
	}

	@Test(invocationCount = 100, threadPoolSize = 10)
	@Parameters({ "apiKey", "apiUri", "requestJsonfile", "maxResponseTimeThreshold" })
	public void testGetProductCatalogueLoad(String apiKey, String apiUri, String requestJsonfile,
			Long maxResponseTimeThreshold)
			throws JsonParseException, JsonMappingException, IOException, JSONException, ParseException {
		logger.info("Running testGetProductCatalogueLoad");
		logger.info("responseTimeThreshold: " + maxResponseTimeThreshold);
		Long startTime = System.currentTimeMillis();
		getProductCatalogue(apiKey, apiUri, requestJsonfile);
		Long duration = System.currentTimeMillis() - startTime;
		assertTrue(duration <= maxResponseTimeThreshold,
				"Response time of GetProductCatalogue call more than: " + maxResponseTimeThreshold);
		logger.info("Completed testGetProductCatalogueLoad");
	}

	@Test
	@Parameters({ "apiKey", "apiUri", "requestJsonfile" })
	public void getProductCatalogue(String apiKey, String apiUri, String requestJsonfile) {
		String url = getUrl(apiUri);
		WebTestClient testClient = WebTestClient.bindToServer().baseUrl(url).build();
		String json = TestAutomationUtil.getContent(requestJsonfile);
		RequestBodyUriSpec requestBody = testClient.post();
		addHeaders(requestBody, apiKey);
		BodyContentSpec body = requestBody.contentType(MediaType.APPLICATION_JSON).syncBody(json).exchange()
				.expectStatus().isOk().expectBody();
	}
}
