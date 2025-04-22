package com.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import io.restassured.response.Response;
import io.restassured.path.json.JsonPath;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.nio.file.*;


import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;


public class ErrorResponse401Test {
    private String baseUrl;
    private String expectedResponse401;
    private Response response;

    @Given("API эмулирует ответ с ошибкой 1002 из файла")
    public void mockApiErrorResponseFromFile() throws IOException {
        expectedResponse401 = new String(Files.readAllBytes(
                Paths.get("src/test/resources/expected_responses/weather_error.json")
        ));
        stubFor(get(urlPathEqualTo("/v1/current.json"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectedResponse401)
                ));
        baseUrl = "http://localhost:" + WireMockTestConfig.wireMockServer.port();
    }

    @When("Я отправляю GET-запрос на weather API")
    public void sendRequestToWeatherApi() {
        response = given()
                .baseUri(baseUrl)
                .queryParam("q", "bulk")
                .queryParam("lang", "ru")
                .queryParam("key", "123")
                .when()
                .get("/v1/current.json");
    }

    @Then("Ответ соответствует структуре из файла weather_error")
    public void verifyResponseStructureError401() {
        JsonPath expectedJson = new JsonPath(expectedResponse401);
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

    @Then("Ответ точно соответствует JSON из файла weather_error")
    public void verifyExactJsonMatch() throws JSONException {
        JSONAssert.assertEquals(
                expectedResponse401,
                response.getBody().asString(),
                false
        );
    }
}