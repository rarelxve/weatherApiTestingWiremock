package com.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;


public class errorResponse400 {
    private Response response;
    private String baseUrl;
    private String expectedResponse400;

    @Given("API эмулирует ответ с ошибкой 1003 из файла")
    public void mockApiErrorResponseFromFile() throws IOException {
        expectedResponse400 = new String(Files.readAllBytes(
                Paths.get("src/test/resources/expected_responses/weather_response_q_is_missing.json")
        ));

        stubFor(get(urlPathEqualTo("/v1/current.json"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectedResponse400)
                ));

        baseUrl = "http://localhost:" + WireMockTestConfig.wireMockServer.port();
    }

    @When("Я отправляю GET-запрос на weather API без параметра q")
    public void sendRequestToWeatherApiWithoutQ() {
        response = given()
                .baseUri(baseUrl)
                .queryParam("lang", "ru")
                .queryParam("key", "74c442b273ce46a1832121006252004")
                .when()
                .get("/v1/current.json");
    }

    @Then("Ответ соответствует структуре из файла weather_response_q_is_missing")
    public void verifyResponseStructureError400() {
        JsonPath expectedJson = new JsonPath(expectedResponse400);
        JsonPath actualJson = response.jsonPath();

        Assertions.assertEquals(
                expectedJson.getInt("error.code"),
                actualJson.getInt("error.code"),
                "Код ошибки совпадает, равен" + actualJson.getInt("error.code")
        );

        Assertions.assertEquals(
                expectedJson.getString("error.message"),
                actualJson.getString("error.message"),
                "Сообщение об ошибке совпадает"
        );
    }

    @Then("Ответ точно соответствует JSON из файла weather_response_q_is_missing")
    public void verifyExactJsonMatch() throws JSONException {
        JSONAssert.assertEquals(
                expectedResponse400,
                response.getBody().asString(),
                false
        );
    }
}