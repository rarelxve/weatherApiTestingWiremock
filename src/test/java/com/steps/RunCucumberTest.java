package com.steps;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.steps",
        plugin = {
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
        }
)
public class RunCucumberTest {
}