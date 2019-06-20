/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */

/*
 * Demonstrates how to invoke another lambda with binary payload and receive results from it
 */

package com.amazonaws.greengrass.examples;

import java.nio.ByteBuffer;
import java.util.Base64;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.greengrass.javasdk.model.*;
import com.amazonaws.greengrass.javasdk.LambdaClient;

public class Invoker {
    private LambdaClient lambdaClient = new LambdaClient();

    public void handleRequest(Object input, Context context) {
        try {
            final InvokeRequest invokeRequest = new InvokeRequest()
                .withFunctionArn("arn:aws:lambda:<region>:<accountId>:function:<targetFunctionName>:<targetFunctionQualifier>")
                .withInvocationType(InvocationType.RequestResponse)
                .withClientContext(Base64.getEncoder().encodeToString("{\"custom\":{\"customData\":\"customData\"}}".getBytes()))
                .withPayload(ByteBuffer.wrap("abcdefg".getBytes("UTF-8")));

            final InvokeResponse response = lambdaClient.invoke(invokeRequest);
            final byte[] bytes = response.getPayload().array();
            System.out.println("Result received: " + new String(bytes));
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
