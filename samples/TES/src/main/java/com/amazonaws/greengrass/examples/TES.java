/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 */

/*
 * Demonstrates retrieving a Group Role Credential if one is set.
 */

package com.amazonaws.greengrass.examples;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.lambda.runtime.Context;

public class TES {
    static {
        Timer timer = new Timer();
        // Schedule a task to run 5 seconds later.
        System.out.println("TES executed");
        timer.schedule(new GetCredential(), 5000);
    }

    public String handleRequest(Object input, Context context) {
        return "Hello from java";
    }
}

class GetCredential extends TimerTask {
    public void run() {
        while (true) {
            try {
                AWSCredentialsProviderChain awsCredentialsProviderChain = new DefaultAWSCredentialsProviderChain();
                BasicSessionCredentials awsCredentials = (BasicSessionCredentials)awsCredentialsProviderChain.getCredentials();
                System.out.println("Credential retrieved");
                System.out.println(awsCredentials.getAWSAccessKeyId());
                System.out.println(awsCredentials.getAWSSecretKey());
                System.out.println(awsCredentials.getSessionToken());
                break;
            } catch (Exception ex) {
                // If retrieving credentials was not successful, sleep for 5 seconds then try again
                System.err.println(ex);
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException iex) {
                    System.out.println(iex);
                }
            }
        }
    }
}
