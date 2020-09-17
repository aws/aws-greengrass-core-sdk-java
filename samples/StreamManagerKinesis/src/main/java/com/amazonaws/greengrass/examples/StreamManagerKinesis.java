/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */

/*
 * This example will create a Greengrass StreamManager stream called "SomeStream".
 * It will then start writing data into that stream and StreamManager will
 * automatically export the written data to a Kinesis Data Stream called "MyKinesisStream".
 * This example will run forever, until the program is killed.
 *
 * The size of the StreamManager stream on disk will not exceed the default (which is 256 MB).
 * Any data appended after the stream reaches the size limit, will continue to be appended, and
 * StreamManager will delete the oldest data until the total stream size is back under 256MB.
 * The Kinesis Data Stream in the cloud has no such bound, so all the data from this script
 * will be uploaded to Kinesis and you will be charged for that usage.
 */

package com.amazonaws.greengrass.examples;

import com.amazonaws.greengrass.javasdk.GreengrassClientBuilder;
import com.amazonaws.greengrass.streammanager.client.StreamManagerClient;
import com.amazonaws.greengrass.streammanager.client.exception.StreamManagerException;
import com.amazonaws.greengrass.streammanager.model.MessageStreamDefinition;
import com.amazonaws.greengrass.streammanager.model.ReadMessagesOptions;
import com.amazonaws.greengrass.streammanager.model.StrategyOnFull;
import com.amazonaws.greengrass.streammanager.model.export.ExportDefinition;
import com.amazonaws.greengrass.streammanager.model.export.KinesisConfig;
import com.amazonaws.services.lambda.runtime.Context;

import java.util.ArrayList;
import java.util.Random;

public class StreamManagerKinesis {
    private static final String STREAM_NAME = "SomeStream";
    private static final String KINESIS_STREAM_NAME = "MyKinesisStream";

    /**
     * Empty handler because this will be a pinned lambda
     */
    public String handleRequest(Object input, Context context) {
        return "Hello from java";
    }

    static {
        try (final StreamManagerClient client = GreengrassClientBuilder.streamManagerClient().build()) {
            // Try deleting the stream (if it exists) so that we have a fresh start
            try {
                client.deleteMessageStream(STREAM_NAME);
            } catch (StreamManagerException e) {
                System.out.println(e.getMessage());
                for(StackTraceElement element: e.getStackTrace()) {
                    System.out.println(element);
                }
            }

            final ExportDefinition exports = new ExportDefinition()
                    .withKinesis(new ArrayList<KinesisConfig>() {{
                        add(new KinesisConfig()
                                .withIdentifier("KinesisExport" + STREAM_NAME)
                                .withBatchSize(1L)
                                .withKinesisStreamName(KINESIS_STREAM_NAME));
                    }});

            client.createMessageStream(
                    new MessageStreamDefinition()
                            .withName(STREAM_NAME)
                            .withStrategyOnFull(StrategyOnFull.OverwriteOldestData)
                            .withExportDefinition(exports));

            // Append 2 messages and print their sequence numbers
            System.out.println(
                    String.format("Successfully appended message to stream with sequence number %d",
                            client.appendMessage(STREAM_NAME, "ABCDEFGHIJKLMNO".getBytes())));
            System.out.println(
                    String.format("Successfully appended message to stream with sequence number %d",
                            client.appendMessage(STREAM_NAME, "PQRSTUVWXYZ".getBytes())));

            // Try reading the 2 messages we just appended and print them out
            System.out.println(String.format("Successfully read 2 messages: %s", client.readMessages(STREAM_NAME, new ReadMessagesOptions().withMinMessageCount(2L).withReadTimeoutMillis(1000L))));

            System.out.println("Now going to start writing random integers between 0 and 255 to the stream");

            // Now start putting in random data between 0 and 255 to emulate device sensor input
            Random rand = new Random(0);
            while (true) {
                System.out.println("Appending new random integer to stream");
                client.appendMessage(STREAM_NAME, new byte[]{(byte) rand.nextInt(255)});
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            for(StackTraceElement element: e.getStackTrace()) {
                System.out.println(element);
            }
            // Properly handle exception
        }
    }
}
