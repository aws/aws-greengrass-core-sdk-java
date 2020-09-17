/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */

/*
 * This example will create a Greengrass StreamManager stream called "SomeStream".
 * It will then start writing data into that stream and StreamManager will
 * automatically export the written data to the customer-created property alias.
 * The property alias should be created before running this example.
 * This example will run forever, until the program is killed.

 * The size of the StreamManager stream on disk will not exceed the default (which is 256 MB).
 * Any data appended after the stream reaches the size limit, will continue to be appended, and
 * StreamManager will delete the oldest data until the total stream size is back under 256MB.
 */

package com.amazonaws.greengrass.examples;

import com.amazonaws.greengrass.javasdk.GreengrassClientBuilder;
import com.amazonaws.greengrass.streammanager.client.StreamManagerClient;
import com.amazonaws.greengrass.streammanager.client.exception.StreamManagerException;
import com.amazonaws.greengrass.streammanager.client.utils.ValidateAndSerialize;
import com.amazonaws.greengrass.streammanager.model.MessageStreamDefinition;
import com.amazonaws.greengrass.streammanager.model.StrategyOnFull;
import com.amazonaws.greengrass.streammanager.model.export.ExportDefinition;
import com.amazonaws.greengrass.streammanager.model.export.IoTSiteWiseConfig;
import com.amazonaws.greengrass.streammanager.model.sitewise.AssetPropertyValue;
import com.amazonaws.greengrass.streammanager.model.sitewise.PutAssetPropertyValueEntry;
import com.amazonaws.greengrass.streammanager.model.sitewise.Quality;
import com.amazonaws.greengrass.streammanager.model.sitewise.TimeInNanos;
import com.amazonaws.greengrass.streammanager.model.sitewise.Variant;
import com.amazonaws.services.lambda.runtime.Context;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class StreamManagerIotSiteWise {
    private static final String STREAM_NAME = "SomeStream";
    private static final String PROPERTY_ALIAS = "SomePropertyAlias";

    /**
     * Empty handler because this will be a long-lived lambda
     */
    public String handleRequest(Object input, Context context) {
        return "";
    }

    static {
        try (final StreamManagerClient client = GreengrassClientBuilder.streamManagerClient().build()) {
            // Try deleting the stream (if it exists) so that we have a fresh start
            try {
                client.deleteMessageStream(STREAM_NAME);
            } catch (StreamManagerException ignored) {
            }

            final ExportDefinition exports = new ExportDefinition()
                    .withIotSitewise(new ArrayList<IoTSiteWiseConfig>() {{
                        add(new IoTSiteWiseConfig()
                                .withIdentifier(UUID.randomUUID().toString()) // Required
                                .withBatchSize(5L));
                    }});

            // Then create the stream with the IoTSiteWise export definition.
            client.createMessageStream(
                    new MessageStreamDefinition()
                            .withName(STREAM_NAME)
                            .withStrategyOnFull(StrategyOnFull.OverwriteOldestData)
                            .withExportDefinition(exports));

            // Now start putting in random site wise entries.
            while (true) {
                PutAssetPropertyValueEntry putAssetPropertyValueEntry = getRandomPutAssetPropertyValueEntry();
                System.out.println(
                        String.format("Successfully appended message to stream with sequence number %d",
                                client.appendMessage(STREAM_NAME, ValidateAndSerialize.validateAndSerializeToJsonBytes(putAssetPropertyValueEntry))));
                // Sleeping for 5 seconds before trying to send another IoTSiteWise entry.
                Thread.sleep(Duration.ofSeconds(1).toMillis());
            }

            } catch (Exception e) {
            // Properly handle exception
            System.out.println(e.getMessage());
            for (StackTraceElement element: e.getStackTrace()) {
                System.out.println(element);
            }
        }
    }

    /**
     * This function will create a random asset property value entry and return it to the caller.
     *
     * @return random PutAssetPropertyValueEntry object.
     */
    private static PutAssetPropertyValueEntry getRandomPutAssetPropertyValueEntry() {
        Random rand = new Random();
        // Note: Inorder to create a new asset property data, you should use the classes defined in the
        // com.amazonaws.greengrass.streammanager.model.sitewise package.
        List<AssetPropertyValue> entries = new ArrayList<>() ;

        // IoTSiteWise requires unique timestamps in all messages and also needs timstamps not earlier
        // than 10 mins in the past. Add some randomness to time and offset.
        final int maxTimeRandomness = 60;
        final int maxOffsetRandomness = 10000;
        double randomValue = rand.nextDouble();
        TimeInNanos timestamp = new TimeInNanos()
                .withTimeInSeconds(Instant.now().getEpochSecond() - rand.nextInt(maxTimeRandomness))
                .withOffsetInNanos((long) (rand.nextInt(maxOffsetRandomness)));
        AssetPropertyValue entry = new AssetPropertyValue()
                .withValue(new Variant().withDoubleValue(randomValue))
                .withQuality(Quality.GOOD)
                .withTimestamp(timestamp);
        entries.add(entry);

        return new PutAssetPropertyValueEntry()
                .withEntryId(UUID.randomUUID().toString())
                .withPropertyAlias(PROPERTY_ALIAS)
                .withPropertyValues(entries);
    }
}