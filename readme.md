# AWS Greengrass Core SDK for Java

The **AWS Greengrass Core SDK for Java** enables Java developers to develop Lambda functions which will run within Greengrass.

## Overview

This document provides instructions for preparing your Greengrass Core environment to run Lambda functions written in Java. It also includes examples on how to develop a Lambda function in Java as well as packaging and running an example Hello World file in Java for your Greengrass core.

## Changes to 1.5.0
*  Stream manager supports automatic data export to AWS S3 and AWS IoT SiteWise, provides new API method to update existing streams, and pause or resume exporting.

## Changes to 1.4.1

*   StreamManager client sets socket option `TCP_NODELAY=true` to prevent multi-millisecond delays when writing small messages.

## Changes to 1.4.0

*   SDK supports StreamManager client.

## Changes to 1.3.1

*   Improved log level granularity.

## Changes to 1.3.0

*   SDK supports SecretsManager client.

## Changes to 1.2.0

*   SDK and GGC compatibility check takes place in the background.

## Changes to 1.1.0

*   You can now invoke lambda with binary data type. Please refer to the examples folder.

## Preparing your Greengrass to run Java Lambda functions

The environment where Greengrass is running on needs to be able to run Java 8 packages.

*   Install Java 8 for your platform. The method will be different based on your platform.
*   Installation of Java 8 will create _**java**_ or _**java8**_ executable in _**/usr/bin**_ or _**/usr/local/bin**_.
*   If the file name is _**java**_, rename it or copy it as _**java8**_ in _**/usr/bin**_ or _**/usr/local/bin**_ folder.
*   Make sure the file is not a symlink.

## Getting Started - Hello World

*   Copy `samples/HelloWorld` folder to your workspace.
*   Create `libs` folder within `HelloWorld` folder and copy `GreengrassJavaSDK.jar`/`GreengrassJavaSDK-Slim.jar` file from `sdk`/`slim-sdk` folder into 
    the `libs` folder.
*   Run `gradle build`
*   You should see a `HelloWorld.zip` in `build/distributions` folder.
*   Go to AWS Lambda Console.
*   Create a new function.
*   Choose the Runtime as `Java 8`.
*   Upload `HelloWorld.zip` file in _Lambda function code_ section.
*   Handler is `com.amazonaws.greengrass.examples.HelloWorld::handleRequest`. The format of handler for java functions is `package.class::method-reference`.
*   Choose any role as the role is not used within Greengrass.
*   After creating the function, publish the Lambda.
*   Create an Alias and point to the Published version (not $LATEST).
*   Go to your Greengrass Group and add the Lambda under Lambdas section.
*   Click on the Lambda that was just added and modify the configuration.
    *   Change the _Lambda lifecycle_ to _Make this function long-lived and keep it running indefinitely._.
    *   Change the _Memory limit_ to at least _64 MB_.
*   Add a Subscription with the following configuration:
    *   Source: Lambda which you just created and added to the group.
    *   Target: IoT Cloud
    *   Topic: hello/world
*   Deploy. A message from your Lambda should be published to the topic _hello/world_ in the cloud every 5 seconds. You can check this by going to AWS IoT's _Test_ page and subscribing to topic _hello/world_.

## Creating a .zip Deployment Package

You can use any building and packaging tool you like to create this zip. Regardless of the tools you use, the resulting .zip file must have the following structure:

*   All compiled class files and resource files at the root level.
*   All required jars to run the code in the `/lib` directory.

All the examples and instructions in this manual use Gradle build and deployment tool to create the .zip.

### Downloading Gradle

You will need to download Gradle. For instructions, go to the gradle website, [https://gradle.org/](https://gradle.org)

### Including Greengrass Core SDK for Java with your function with Gradle

Two types of jar files are provided. The `GreengrassJavaSDK-Slim.jar` in `sdk-slim` directory is a jar file with 
no dependencies built in. This is the recommended way to consume Greengrass Core SDK. `GreengrassJavaSDK.jar` in 
`sdk` directory is provided for backward compatibility for developers already using it.

For `GreengrassJavaSDK-Slim.jar`, follow the example below:

*   Create `libs` folder.
*   Copy `GreengrassJavaSDK-Slim.jar` to `libs` folder.
*   Example `build.gradle` file for Greengrass function looks like the following. You may add additional dependencies as necessary for your function.  

    ```java  

    repositories {  
        mavenCentral()  
    }  

    dependencies {
        compile 'com.fasterxml.jackson.core:jackson-annotations:2.12.3'
        compile 'com.fasterxml.jackson.core:jackson-core:2.12.3'
        compile 'com.fasterxml.jackson.core:jackson-databind:2.12.3'
        compile 'com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.12.3'
        compile 'org.apache.httpcomponents:httpclient:4.5.13'
        compile 'org.apache.httpcomponents:httpcore:4.4.14'
        compile 'com.amazonaws:aws-lambda-java-core:1.1.0'
        compile 'javax.validation:validation-api:1.0.0.GA'
        compile 'org.slf4j:slf4j-api:1.7.0'
        compile fileTree(dir: 'libs', include: ['*.jar'])  
    }  

    task buildZip(type: Zip) {  
        from compileJava  
        from processResources  
        into('lib') {  
            from configurations.runtime  
        }  
    }  

    build.dependsOn buildZip  

    ```

*   Place your lambda function under `src` folder.
*   Run `gradle build`
*   This should place a zipped file of your function under `build/distributions` folder which you can now upload to AWS Lambda to be used by Greengrass.

## Logging in Java Lambdas

Your _System.out.println_ operation will be logged as INFO. A _System.err.println_ operation will be logged as ERROR. Alternatively, you can also log using `context.getLogger().log` operation which will log at INFO level. Currently, our Java SDK only allows you to log at INFO or ERROR level only.

## Supported Datatypes

From GGC version 1.5.0, you can send binary data with the SDK. However, in order to make a lambda function be able to handle binary payload. You need to do the following:

*   Make sure to choose "binary" input payload type in lamba configuration page in Greengrass condole and then do a deployment. Your lambda function will be marked as a "binary" lambda by GGC.
*   Make sure your lambda handler signature is one of the following:
    ```java  
    void (InputStream, OutputStream, Context)  
    void (InputStream, OutputStream)  
    void (OutputStream, Context)  
    void (InputStream, Context)  
    void (InputStream)  
    void (OutputStream)
    ```

## Supported Function Signatures

In addition to the function signatures mentioned above, there are also more supported function signatures being introduced:

Supported "json" function handler signatures:
```java
    Anything (Context)  
    Anything (AlmostAnything, Context)  
    Anything (AlmostAnything)  
    Anything ()
```

## Handler Overload Resolution

If your Java code contains multiple methods with same name as the handler name, then GGC uses the following rules to pick a method to invoke:

*   Select the method with the largest number of parameters.
*   If two or more methods have the same number of parameters, GGC selects the method that has the Context as the last parameter.

## Supported Context

In Greengrass, you can send a context object in a JSON format to be passed to another Lambda that is being invoked. The context format looks like this: { custom: { customData: 'customData', }, }

<div class="section" id="compatibility">

## Compatibility

As new features are added to AWS IoT Greengrass, newer versions of the AWS IoT Greengrass SDK may be incompatible with older versions of the AWS IoT Greengrass core. The following table lists the compatible SDKs for all GGC releases.

| GGC Version   | Compatible SDK Versions |
| ------------- | ------------- |
| 1.0.x-1.6.x   | 1.0.x         |
| 1.7.x-1.9.x   | 1.0.x-1.3.x   |
| 1.10.x        | 1.0.x-1.4.x   |
| 1.11.x        | 1.0.x-1.5.x   |
