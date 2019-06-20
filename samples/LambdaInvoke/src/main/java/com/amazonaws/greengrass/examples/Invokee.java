/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */

/*
 * Demonstrates how to invoke another lambda and receive a value from the call
 */

package com.amazonaws.greengrass.examples;

import com.amazonaws.services.lambda.runtime.Context;

public class Invokee {
    public Object handleRequest(Payload input, Context context) {
        System.out.println(input.getMessage());
        System.out.println(context.getClientContext().getCustom().toString());

        Payload returnPayload = new Payload();
        returnPayload.setMessage("return message");

        return returnPayload;
    }
}

