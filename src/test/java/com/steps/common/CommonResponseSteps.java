package com.steps.common;

import io.cucumber.java.en.Then;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.skyscreamer.jsonassert.JSONAssert;

public class CommonResponseSteps {
    protected Response response;
    protected String expectedResponse;

    @Then("Ответ соответствует структуре из файла")
    public void verifyResponseStructure() {
        JsonPath expectedJson = new JsonPath(expectedResponse);
        JsonPath actualJson = response.jsonPath();

        Assertions.assertEquals(
                expectedJson.getInt("error.code"),
                actualJson.getInt("error.code"),
                "Код ошибки совпадает"
        );

        Assertions.assertEquals(
                expectedJson.getString("error.message"),
                actualJson.getString("error.message"),
                "Сообщение об ошибке совпадает"
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