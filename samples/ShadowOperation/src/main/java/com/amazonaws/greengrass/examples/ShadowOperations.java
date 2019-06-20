/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */

/*
 * Demonstrates a shadow operation using Greengrass Core Java SDK
 * This lambda function will retrieve underlying platform information and
 * update the reported state of 'javaPlatform' shadow with that message.
 */

package com.amazonaws.greengrass.examples;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.greengrass.javasdk.IotDataClient;
import com.amazonaws.greengrass.javasdk.model.*;

public class ShadowOperations {
    private IotDataClient iotDataClient = new IotDataClient();
    private String platform = String.format("%s-%s", System.getProperty("os.name"), System.getProperty("os.version"));
    private String shadowPayload = String.format("{\"state\":{\"reported\":{\"platform\":\"%s\"}}}", platform);

    public String handleRequest(Object input, Context context) {
        // Update Thing Shadow
        UpdateThingShadowRequest updateThingShadowRequest = new UpdateThingShadowRequest()
            .withThingName("javaPlatform").withPayload(ByteBuffer.wrap(shadowPayload.getBytes()));

        try {
            UpdateThingShadowResult result = iotDataClient.updateThingShadow(updateThingShadowRequest);
            System.out.println(new String(result.getPayload().array(), "UTF-8"));
        } catch (GGIotDataException|UnsupportedEncodingException ex) {
            System.err.println(ex);
        }

        // Get Thing Shadow
        GetThingShadowRequest getThingShadowRequest = new GetThingShadowRequest().withThingName("javaPlatform");

        try {
            GetThingShadowResult result = iotDataClient.getThingShadow(getThingShadowRequest);
            System.out.println(new String(result.getPayload().array(), "UTF-8"));
        } catch (GGIotDataException|UnsupportedEncodingException ex) {
            System.err.println(ex);
        }

        return "Shadow Operation";
    }
}
