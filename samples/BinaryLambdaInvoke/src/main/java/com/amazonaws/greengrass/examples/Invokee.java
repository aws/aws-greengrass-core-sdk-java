/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */

/*
 * Demonstrates how to use a lamda with "binary" encoding type to handle byte streams
 */

package com.amazonaws.greengrass.examples;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.amazonaws.services.lambda.runtime.Context;

public class Invokee {
    // A lambda handler deals with streams must have "binary" encoding type
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        int letter = 0;
        while((letter = inputStream.read()) >= 0) {
            outputStream.write(Character.toUpperCase(letter));
        }
    }

    // This handler has the same name as above, however, it will not be invoked because it has shorter function signature
    public void handleRequest(InputStream inputStream, OutputStream outputStream) throws Exception {
        int letter = 0;
        while((letter = inputStream.read()) >= 0) {
            outputStream.write(letter);
        }
    }
}
