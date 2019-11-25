/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */

/*
 * Demonstrates a simple publish to a topic using Greengrass Core Java SDK
 * This lambda function will retrieve underlying platform information and send
 * a hello world message along with the platform information to the topic
 * 'hello/world'. The function will sleep for five seconds, then repeat.
 * Since the function is long-lived it will run forever when deployed to a
 * Greengrass core.
 */

package com.amazonaws.greengrass.examples;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.greengrass.javasdk.IotDataClient;
import com.amazonaws.greengrass.javasdk.model.*;

public class HelloWorld {
    static {
        Timer timer = new Timer();
        // Repeat publishing a message every 5 seconds
        timer.scheduleAtFixedRate(new PublishHelloWorld(), 0, 5000);
    }

    public String handleRequest(Object input, Context context) {
        return "Hello from java";
    }
}

class PublishHelloWorld extends TimerTask {
    private IotDataClient iotDataClient = new IotDataClient();
    private String publishMessage = String.format("Hello world! Sent from Greengrass Core running on platform: %s-%s using Java", System.getProperty("os.name"), System.getProperty("os.version"));
    private PublishRequest publishRequest = new PublishRequest()
            .withTopic("hello/world")
            .withPayload(ByteBuffer.wrap(String.format("{\"message\":\"%s\"}", publishMessage).getBytes()))
            .withQueueFullPolicy(QueueFullPolicy.AllOrException);

    public void run() {
        try {
            iotDataClient.publish(publishRequest);
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }
}
