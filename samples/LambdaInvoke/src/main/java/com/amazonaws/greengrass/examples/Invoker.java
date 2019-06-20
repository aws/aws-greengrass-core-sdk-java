/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */

/*
 * Demonstrates how to invoke another lambda and receive a value from the call
 */

package com.amazonaws.greengrass.examples;

import java.nio.ByteBuffer;
import java.util.Base64;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.greengrass.javasdk.model.*;
import com.amazonaws.greengrass.javasdk.LambdaClient;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Invoker {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private LambdaClient lambdaClient = new LambdaClient();

    public void handleRequest(Object input, Context context) {
        try {
            final InvokeRequest invokeRequest = new InvokeRequest()
                .withFunctionArn("arn:aws:lambda:<region>:<accountId>:function:<targetFunctionName>:<targetFunctionQualifier>")
                .withInvocationType(InvocationType.RequestResponse)
                .withClientContext(Base64.getEncoder().encodeToString("{\"custom\":{\"customData\":\"customData\"}}".getBytes()))
                .withPayload(ByteBuffer.wrap("{\"message\":\"payload message\"}".getBytes("UTF-8")));

            final InvokeResponse response = lambdaClient.invoke(invokeRequest);
            final Payload responseMessage = OBJECT_MAPPER.readValue(response.getPayload().array(), Payload.class);
            System.out.println(responseMessage.getMessage());
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}

