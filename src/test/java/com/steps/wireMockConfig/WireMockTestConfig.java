package com.steps.wireMockConfig;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.cucumber.java.After;
import io.cucumber.java.Before;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class WireMockTestConfig {
    public static final WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());

    @Before
    public void startWireMock() {
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @After
    public void stopWireMock() {
        wireMockServer.stop();
    }
}