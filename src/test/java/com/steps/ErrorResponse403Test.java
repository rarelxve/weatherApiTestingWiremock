package com.steps;

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


public class ErrorResponse403Test {
    private Response response;
    private String baseUrl;
    private String expectedResponse403;

    @Given("API эмулирует ответ с ошибкой 2008 из файла")
    public void mockApiErrorResponseFromFile() throws IOException {
        expectedResponse403 = new String(Files.readAllBytes(
                Paths.get("src/test/resources/expected_responses/weather_response_q_is_missing.json")
        ));

        stubFor(get(urlPathEqualTo("/v1/current.json"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectedResponse403)
                ));

        baseUrl = "http://localhost:" + WireMockTestConfig.wireMockServer.port();
    }

    @When("Я отправляю GET-запрос на weather API c некорректным ключом")
    public void sendRequestToWeatherApiWithIncorrectKey() {
        response = given()
                .baseUri(baseUrl)
                .queryParam("q", "bulk")
                .queryParam("lang", "ru")
                .queryParam("key", "74c442b273ce46a1832121006252003")
                .when()
                .get("/v1/current.json");
    }

    @Then("Ответ соответствует структуре из файла weather_error_response_code_403")
    public void verifyResponseStructureError403() {
        JsonPath expectedJson = new JsonPath(expectedResponse403);
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

    @Then("Ответ точно соответствует JSON из файла weather_error_response_code_403")
    public void verifyExactJsonMatch() throws JSONException {
        JSONAssert.assertEquals(
                expectedResponse403,
                response.getBody().asString(),
                false
        );
    }
}