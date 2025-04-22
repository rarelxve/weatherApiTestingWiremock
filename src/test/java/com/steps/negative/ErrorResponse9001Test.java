package com.steps.negative;

import com.steps.wireMockConfig.WireMockTestConfig;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ErrorResponse9001Test {
    private Response response;
    private String baseUrl;
    private String expectedResponse9001;
    private String generatedRequestBody;

    @Before
    public void generateRequestBody() {
        generatedRequestBody = generateBulkRequest();
    }

    @Given("API эмулирует ответ с ошибкой 9001 из файла")
    public void mockApiErrorResponseFromFile() throws IOException {
        expectedResponse9001 = new String(Files.readAllBytes(
                Paths.get("src/test/resources/expected_responses/weather_error_response_code_9001.json")
        ));

        stubFor(post(urlPathEqualTo("/v1/current.json"))
                .withQueryParam("q", equalTo("bulk"))
                .withQueryParam("lang", equalTo("ru"))
                .withQueryParam("key", equalTo("74c442b273ce46a1832121006252004"))
                .withRequestBody(equalToJson(generatedRequestBody))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody(expectedResponse9001))
        );

        baseUrl = "http://localhost:" + WireMockTestConfig.wireMockServer.port();
    }

    @When("Я отправляю POST-запрос с 51 городом, превышая лимит API")
    public void sendRequestToWeatherApiWithoutQ() {
        response = given()
                .log().all()
                .baseUri(baseUrl)
                .queryParam("q", "bulk")
                .queryParam("lang", "ru")
                .queryParam("key", "74c442b273ce46a1832121006252004")
                .contentType("application/json")
                .body(generatedRequestBody)
                .when()
                .post("/v1/current.json");
    }

    @Then("Ответ соответствует структуре из файла weather_error_response_code_9001.json")
    public void verifyResponseStructureError9001() {

        System.out.println("Raw response: " + response.getBody().asString());

        JsonPath expectedJson = new JsonPath(expectedResponse9001);
        JsonPath actualJson = response.jsonPath();

        assertEquals(
                "Код ошибки совпадает, равен " + actualJson.getInt("error.code"),
                expectedJson.getInt("error.code"),
                actualJson.getInt("error.code")
        );

        assertEquals(
                "Сообщение об ошибке совпадает",
                expectedJson.getString("error.message"),
                actualJson.getString("error.message")
        );
    }

    @Then("Ответ точно соответствует JSON из файла weather_error_response_code_9001.json")
    public void verifyExactJsonMatch() throws JSONException {
        JSONAssert.assertEquals(
                expectedResponse9001,
                response.getBody().asString(),
                false
        );
    }


    private String generateBulkRequest() {
        List<String> cities = IntStream.rangeClosed(1, 51)
                .mapToObj(i -> "Рандомный город номер " + i)
                .toList();
        String locations = cities.stream()
                .map(city -> "{\"q\":\"" + city + "\"}")
                .collect(Collectors.joining(","));
        return "{ \"locations\": [" + locations + "] }";
    }
}