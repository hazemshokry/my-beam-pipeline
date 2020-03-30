/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springml.pipelines;
//import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.values.PCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A starter example for writing Beam programs.
 *
 * <p>The example takes two strings, converts them to their upper-case
 * representation and logs them.
 *
 * <p>To run this starter example locally using DirectRunner, just
 * execute it without any additional parameters from your favorite development
 * environment.
 *
 * <p>To run this starter example using managed resource in Google Cloud
 * Platform, you should specify the following command-line options:
 *   --project=<YOUR_PROJECT_ID>
 *   --stagingLocation=<STAGING_LOCATION_IN_CLOUD_STORAGE>
 *   --runner=DataflowRunner
 */
// Test commit3
public class StarterPipeline {
  private static final Logger LOG = LoggerFactory.getLogger(StarterPipeline.class);

  public interface Options extends PipelineOptions {

    @Description("Input GCS File Path")
    @Default.String("src/covid_19_data.csv")
    ValueProvider<String> getGCSFilePath();
    void setGCSFilePath(ValueProvider<String> value);

    @Description("Output PubSub Topic")
    @Default.String("projects/myspringml2/topics/cicd-test")
    ValueProvider<String> getOutputTopic();
    void setOutputTopic(ValueProvider<String> value);
  }

  public static void main(String[] args) {

    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);

    Pipeline p = Pipeline.create(options);
    PCollection<String> input = p.apply("Read Records", TextIO.read().from(options.getGCSFilePath()));


    input.apply("Publish to PubSub", PubsubIO.writeStrings()
            .to(options.getOutputTopic()));
    p.run();
  }
}
