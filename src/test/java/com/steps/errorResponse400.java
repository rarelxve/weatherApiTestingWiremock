package com.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import io.restassured.path.json.JsonPath;
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
    private String expectedResponse;

    @Given("API эмулирует ответ с ошибкой 1002 из файла")
    public void mockApiErrorResponseFromFile() throws IOException {
        // Читаем ожидаемый ответ из файла
        expectedResponse = new String(Files.readAllBytes(
                Paths.get("src/test/resources/expected_responses/weather_error.json")
        ));

        // Настраиваем заглушку
        stubFor(get(urlPathEqualTo("/v1/current.json"))
                .willReturn(aResponse()
                        .withStatus(200)
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
                .queryParam("key", "invalid_key")
                .when()
                .get("/v1/current.json");
    }

    @Then("Ответ соответствует структуре из файла")
    public void verifyResponseStructure() {
        // Сравниваем JSON-структуры (без учета пробелов и порядка полей)
        JsonPath expectedJson = new JsonPath(expectedResponse);
        JsonPath actualJson = response.jsonPath();

        Assertions.assertEquals(
                expectedJson.getInt("error.code"),
                actualJson.getInt("error.code"),
                "Код ошибки не совпадает"
        );

        Assertions.assertEquals(
                expectedJson.getString("error.message"),
                actualJson.getString("error.message"),
                "Сообщение об ошибке не совпадает"
        );
    }

    @Then("Ответ точно соответствует JSON из файла")
    public void verifyExactJsonMatch() throws JSONException {
        JSONAssert.assertEquals(
                expectedResponse,
                response.getBody().asString(),
                false
        );
    }
}