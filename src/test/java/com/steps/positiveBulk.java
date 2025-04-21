package com.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;
public class positiveBulk {
    private Response response;
    private String baseUrl;
    private String expectedResponse;

    @Given("Настроен корректный ответ для bulk-запроса из файла")
    public void setupMockResponse() throws IOException {
        expectedResponse = Files.readString(Paths.get(
                "src/test/resources/expected_responses/bulk_response.json"
        ));

        stubFor(post(urlPathEqualTo("/v1/current.json"))
                .withQueryParam("q", equalTo("bulk"))
                .withQueryParam("lang", equalTo("ru"))
                .withQueryParam("key", equalTo("74c442b273ce46a1832121006252004"))
                .willReturn(okJson(expectedResponse)));
         baseUrl = "http://localhost:" + WireMockTestConfig.wireMockServer.port();
    }

    @When("Я отправляю POST-запрос с четырьмя городами")
    public void sendValidRequest() throws IOException {
        String requestBody = Files.readString(Paths.get(
                "src/test/resources/requests/bulk_request.json"
        ));

        response = given()
                .log().all()
                .baseUri(baseUrl)
                .queryParam("q", "bulk")
                .queryParam("lang", "ru")
                .queryParam("key", "74c442b273ce46a1832121006252004")
                .contentType("application/json")
                .body(requestBody)
                .post("/v1/current.json");
    }

    @Then("Каждый элемент массива имеет корректную структуру")
    public void verifyStructure() {
        JsonPath json = response.jsonPath();
        List<Map<String, Object>> bulk = json.getList("bulk");

        bulk.forEach(item -> {
            Map<String, Object> query = (Map<String, Object>) item.get("query");
            assertAll("Проверка структуры элемента",
                    () -> assertTrue(query.containsKey("q"), "Отсутствует поле 'q'"),
                    () -> assertNotNull(query.get("location"), "Отсутствует объект 'location'"),
                    () -> assertNotNull(query.get("current"), "Отсутствует объект 'current'"),
                    () -> assertTrue(((Map<?, ?>) query.get("location")).containsKey("name"),
                            "Отсутствует поле 'name' в location"),
                    () -> assertTrue(((Map<?, ?>) query.get("current")).containsKey("temp_c"),
                            "Отсутствует поле 'temp_c' в current")
            );
        });
    }


    @Then("Ответ содержит данные по четырем городам")
    public void checkCitiesCount() {
        int actualCount = response.jsonPath().getList("bulk").size();
        Assertions.assertEquals(4, actualCount,
                "Количество городов в ответе должно быть 4");
    }

    @Then("Конкретные параметры городов соответствуют:")
    public void checkCityValues(io.cucumber.datatable.DataTable dataTable) {
        dataTable.asMaps().forEach(row -> {
            String city = row.get("City");
            String field = row.get("Field");
            String expectedValue = row.get("Value");

            String jsonPath = String.format(
                    "bulk.find { it.query.q == '%s' }.query.current.%s",
                    city, field
            );

            String actualValue = response.jsonPath().getString(jsonPath);
            Assertions.assertEquals(expectedValue, actualValue);
        });
    }
}