package com.steps;

import com.steps.common.CommonResponseSteps;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Step;
import io.restassured.response.Response;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;


public class errorResponse401 extends CommonResponseSteps {
    private String baseUrl;

    @Given("API эмулирует ответ с ошибкой 1002 из файла")
    public void mockApiErrorResponseFromFile() throws IOException {
        expectedResponse = new String(Files.readAllBytes(
                Paths.get("src/test/resources/expected_responses/weather_error.json")
        ));
        stubFor(get(urlPathEqualTo("/v1/current.json"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectedResponse)
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

}