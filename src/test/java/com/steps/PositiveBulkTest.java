package com.steps;

import io.cucumber.java.en.*;
import io.restassured.response.Response;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

import io.restassured.path.json.JsonPath;

import java.util.List;
import java.util.Map;


public class PositiveBulkTest {
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

        for (Map<String, Object> item : bulk) {
            Map<String, Object> query = (Map<String, Object>) item.get("query");

            // Проверки заменены на отдельные assertions
            assertTrue("Отсутствует поле 'q'", query.containsKey("q"));
            assertNotNull("Отсутствует объект 'location'", query.get("location"));
            assertNotNull("Отсутствует объект 'current'", query.get("current"));

            Map<?, ?> location = (Map<?, ?>) query.get("location");
            assertTrue("Отсутствует поле 'name' в location", location.containsKey("name"));

            Map<?, ?> current = (Map<?, ?>) query.get("current");
            assertTrue("Отсутствует поле 'temp_c' в current", current.containsKey("temp_c"));
        }
    }

    @Then("Ответ содержит данные по четырем городам")
    public void checkCitiesCount() {
        int actualCount = response.jsonPath().getList("bulk").size();
        assertEquals("Количество городов в ответе должно быть 4", 4, actualCount);
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
            assertEquals(expectedValue, actualValue);
        });
    }
}