/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */

/*
 * Demonstrates how to invoke another lambda and receive a value from the call
 */

package com.amazonaws.greengrass.examples;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Payload {
    public Payload() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("message")
    private String message;
}

