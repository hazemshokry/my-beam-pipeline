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
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Sum;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sample for writing Beam programs.
 *
 * <p>The sample takes covid_19 dataset as an input file location as a string,
 * grouping them by country and output value pairs of <country, count> to three different destinations:
 * Local file, GS bucket and Google Pubsub topic (If provided in the parameters list).
 *
 * <p>The main purpose of this demo to apply CI/CD process using Jenkins, Terraform and SonarQube.
 *
 * <p>To run this starter example locally using DirectRunner, just
 * execute it without any additional parameters from your favorite development
 * environment, you can provide <setGCSFilePath> parameter if you have a different input dataset.
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
    // /Users/hazemsayed/Downloads/novel-corona-virus-2019-dataset/covid_19_data.csv
//    @Default.String("gs://dataflow-cicd/data/input/*")
    ValueProvider<String> getGCSFilePath();
    void setGCSFilePath(ValueProvider<String> value);

    @Description("Output PubSub Topic")
    @Default.String("projects/myspringml2/topics/cicd-test")
    ValueProvider<String> getOutputTopic();
    void setOutputTopic(ValueProvider<String> value);
  }

  public static class FormatCountry extends DoFn<String, KV<String,Integer>> {
    @ProcessElement
    public void processElement(ProcessContext c) throws Exception {
      String[] line = c.element().split(",");
      c.output(KV.of(line[3],1));
    }
  }

  public static class FormatOutput extends DoFn<KV<String, Integer>, String> {
    @ProcessElement
    public void processElement(ProcessContext c) throws Exception {
      String output = c.element().getKey() + "," + c.element().getValue();
      c.output(output);
    }
  }


  public static void main(String[] args) {

    Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);

    Pipeline p = Pipeline.create(options);
    PCollection<String> input = p.apply("Read Records", TextIO.read().from(options.getGCSFilePath())).
            apply("Emit <country, 1>", ParDo.of(new FormatCountry())).
            apply("Group by country", Combine.perKey(Sum.ofIntegers())) .
            apply("Prepare output", ParDo.of(new FormatOutput()));

    input.apply("Write to File", TextIO.write().to("src/covid_19_data_output.csv").withoutSharding());

    input.apply("Write to GS", TextIO.write().to("gs://dataflow-cicd/data/output/"));
    input.apply("Publish to PubSub", PubsubIO.writeStrings()
            .to(options.getOutputTopic()));

    p.run();
  }
}