package com.example.zookeeperusersnodes.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Component
public class CommonUtils {
//    private int serverPort;
//    private List<String> webSocketInstances;

    @Value("${ws.instances}")
    private List<String> webSocketInstances;

    @Value("${server.port}")
    private int serverPort;

//    @PostConstruct
//    public void init() {
//        Environment env = EnvironmentProvider.getEnvironment();
//        if (env != null) {
//            this.serverPort = Integer.parseInt(env.getProperty("server.port", "0"));
//            this.webSocketInstances = Arrays.asList(env.getProperty("ws.instances", "").split(","));
//        } else {
//            throw new IllegalStateException("Environment is not initialized yet");
//        }
//    }

//    public CommonUtils() {
//        Environment env = EnvironmentProvider.getEnvironment();
//
//        this.serverPort = Integer.parseInt(env.getProperty("server.port", "0"));
//        this.webSocketInstances = Arrays.asList(env.getProperty("ws.instances", "").split(","));
//        // Load properties
//        Properties properties = new Properties();
//        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
//            if (input == null) {
//                System.out.println("Sorry, unable to find application.properties");
//                return;
//            }
//            properties.load(input);
//        }
//        catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        // Initialize fields from properties
//        this.serverPort = Integer.parseInt(properties.getProperty("server.port", "0"));
//        this.webSocketInstances = Arrays.asList(properties.getProperty("ws.instances", "").split(","));
//    }

    public String getRemainingWebSocketInstance(List<String> webSocketInstances) {
        List<String> remainingWebSocketInstances = new ArrayList<>(this.webSocketInstances);

        remainingWebSocketInstances.removeAll(webSocketInstances);

        return remainingWebSocketInstances.getFirst();
    }

    public String getWebSocketInstanceByFrontendAddress(String frontendAddress) {
        if(frontendAddress.contains("4200"))
            return webSocketInstances.get(0);
        if(frontendAddress.contains("4201"))
            return webSocketInstances.get(1);
        if(frontendAddress.contains("4202"))
            return webSocketInstances.get(2);

        return null;
    }

    public String getCurrentWebSocketInstance() {
        if(serverPort == 9090)
            return webSocketInstances.get(0);
        if(serverPort == 9091)
            return webSocketInstances.get(1);
        if(serverPort == 9092)
            return webSocketInstances.get(2);

        return null;
    }

    public static String findDifferentElement(List<String> list1, List<String> list2) {
        List<String> copyOfList1 = new ArrayList<>(list1);

        copyOfList1.removeAll(list2);

        if (copyOfList1.size() == 1) {
            return copyOfList1.getFirst();
        } else {
            throw new IllegalStateException("There is no unique different element.");
        }
    }
}
