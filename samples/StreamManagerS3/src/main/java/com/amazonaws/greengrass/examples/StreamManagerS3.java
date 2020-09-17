/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */

/*
 * This example creates a local stream named "SomeStream", and a status stream named "SomeStatusStream.
 * It adds 1 S3 Export task into the "SomeStream" stream and then stream manager automatically exports
 * the data to a customer-created S3 bucket named "SomeBucket".
 * The S3 bucket should be created before running this example.
 * This example runs until the customer-created file at URL "SomeURL" has been uploaded to the S3 bucket.
 */

package com.amazonaws.greengrass.examples;

import com.amazonaws.greengrass.javasdk.GreengrassClientBuilder;
import com.amazonaws.greengrass.streammanager.client.StreamManagerClient;
import com.amazonaws.greengrass.streammanager.client.exception.ResourceNotFoundException;
import com.amazonaws.greengrass.streammanager.client.exception.StreamManagerException;
import com.amazonaws.greengrass.streammanager.client.utils.ValidateAndSerialize;
import com.amazonaws.greengrass.streammanager.model.Message;
import com.amazonaws.greengrass.streammanager.model.MessageStreamDefinition;
import com.amazonaws.greengrass.streammanager.model.ReadMessagesOptions;
import com.amazonaws.greengrass.streammanager.model.S3ExportTaskDefinition;
import com.amazonaws.greengrass.streammanager.model.Status;
import com.amazonaws.greengrass.streammanager.model.StatusConfig;
import com.amazonaws.greengrass.streammanager.model.StatusLevel;
import com.amazonaws.greengrass.streammanager.model.StatusMessage;
import com.amazonaws.greengrass.streammanager.model.StrategyOnFull;
import com.amazonaws.greengrass.streammanager.model.export.ExportDefinition;
import com.amazonaws.greengrass.streammanager.model.export.S3ExportTaskExecutorConfig;
import com.amazonaws.services.lambda.runtime.Context;

import java.util.ArrayList;
import java.util.List;

public class StreamManagerS3 {
    private static final String STREAM_NAME = "SomeStream";
    private static final String STATUS_STREAM_NAME = "SomeStatusStream";
    private static final String BUCKET = "SomeBucket";
    private static final String KEY = "SomeKey";
    private static final String FILE_URL = "file:/path/to/some/file.someExtension";

    /**
     * Empty handler because this will be a pinned lambda
     */
    public String handleRequest(Object input, Context context) {
        return "Hello from java";
    }

    static {
        try (final StreamManagerClient client = GreengrassClientBuilder.streamManagerClient().build()) {
            // Try deleting the status stream (if it exists) so that we have a fresh start
            try {
                client.deleteMessageStream(STATUS_STREAM_NAME);
            } catch (ResourceNotFoundException ignored) {
            }

            // Try deleting the stream (if it exists) so that we have a fresh start
            try {
                client.deleteMessageStream(STREAM_NAME);
            } catch (ResourceNotFoundException ignored) {
            }

            final ExportDefinition exports = new ExportDefinition()
                    .withS3TaskExecutor(new ArrayList<S3ExportTaskExecutorConfig>() {{
                        add(new S3ExportTaskExecutorConfig()
                                .withIdentifier("S3Export" + STREAM_NAME) // Required
                                // Optional. Add an export status stream to add statuses for all S3 upload tasks.
                                .withStatusConfig(new StatusConfig()
                                        .withStatusLevel(StatusLevel.INFO) // Default is INFO level statuses.
                                        // Status Stream should be created before specifying in S3 Export Config.
                                        .withStatusStreamName(STATUS_STREAM_NAME)));
                    }});

            // Create the export status stream first.
            client.createMessageStream(
                    new MessageStreamDefinition()
                            .withName(STATUS_STREAM_NAME)
                            .withStrategyOnFull(StrategyOnFull.OverwriteOldestData));

            // Then create the stream with the S3 Export definition.
            client.createMessageStream(
                    new MessageStreamDefinition()
                            .withName(STREAM_NAME)
                            .withStrategyOnFull(StrategyOnFull.OverwriteOldestData)
                            .withExportDefinition(exports));

            // Append a S3 export task definition and print the sequence number.
            S3ExportTaskDefinition s3ExportTaskDefinition = new S3ExportTaskDefinition()
                    .withBucket(BUCKET)
                    .withKey(KEY)
                    .withInputUrl(FILE_URL);
            System.out.println(
                    String.format("Successfully appended message to stream with sequence number %d",
                            client.appendMessage(STREAM_NAME,
                                    ValidateAndSerialize.validateAndSerializeToJsonBytes(s3ExportTaskDefinition))));

            System.out.println("Now going to start reading statuses from the export status stream.");
            boolean isS3UploadComplete = false;
            while (!isS3UploadComplete) {
                try {
                    // Read the statuses from the export status stream
                    List<Message> messages = client.readMessages(STATUS_STREAM_NAME,
                            new ReadMessagesOptions().withMinMessageCount(1L).withReadTimeoutMillis(1000L));
                    for (Message message : messages) {
                        // Deserialize the status message first.
                        StatusMessage statusMessage = ValidateAndSerialize.deserializeJsonBytesToObj(message.getPayload(), StatusMessage.class);
                        // Check the status of the status message. If the status is "Success", the file was successfully uploaded to S3.
                        // If the status was either "Failure" or "Canceled", the server was unable to upload the file to S3.
                        // We will print the message for why the upload to S3 failed from the status message.
                        // If the status was "InProgress", the status indicates that the server has started uploading the S3 task.
                        if (Status.Success.equals(statusMessage.getStatus())) {
                            System.out.println("Successfully uploaded file at path " + FILE_URL + " to S3.");
                            isS3UploadComplete = true;
                        } else if (Status.Failure.equals(statusMessage.getStatus()) || Status.Canceled.equals(statusMessage.getStatus())) {
                            System.out.println(String.format("Unable to upload file at path %s to S3. Message %s",
                                    statusMessage.getStatusContext().getS3ExportTaskDefinition().getInputUrl(),
                                    statusMessage.getMessage()));
                            isS3UploadComplete = true;
                        }
                    }
                } catch (StreamManagerException ignored) {
                } finally {
                    // Sleep for sometime for the S3 upload task to complete before trying to read the status message.
                    Thread.sleep(5000);
                }
            }
        } catch (Exception ignored) {
        }
    }
}