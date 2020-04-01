package com.springml.pipelines;

import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.ValidatesRunner;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StarterPipelineTest {

    @Rule public TestPipeline p = TestPipeline.create();

    @Test
    @Category(ValidatesRunner.class)
    public void testFormatCountry() throws Exception {

        PCollection<String> input = p.apply(Create.of("8,01/22/2020,Guizhou,Mainland China,1/22/2020 17:00,1,0,0")).
                setCoder(StringUtf8Coder.of());

        PCollection<KV<String, Integer>> output =
                input.apply("Emit <country, 1>", ParDo.of(new StarterPipeline.FormatCountry()));

        PAssert.that(output)
                .containsInAnyOrder(KV.of("Mainland China",1));

        p.run().waitUntilFinish();

    }

    @Test
    @Category(ValidatesRunner.class)
    public void testFormatOutput() throws Exception {

        PCollection <KV<String, Integer>> input = p.apply(Create.of(KV.of("Mainland China",1)));

        PCollection <String> output =
                input.apply("Prepare output", ParDo.of(new StarterPipeline.FormatOutput()));

        PAssert.that(output)
                .containsInAnyOrder("Mainland China,1");

        p.run().waitUntilFinish();
    }

    @Test
    @Category(ValidatesRunner.class)
    public void testPipeline () throws Exception {
        PCollection<String> input = p.apply(Create.of
                ("8,01/22/2020,Guizhou,Mainland China,1/22/2020 17:00,1,0,0",
                        "12,01/22/2020,Henan,Mainland China,1/22/2020 17:00,5,0,0",
                        "26,01/22/2020,Shanghai,Mainland China,1/22/2020 17:00,9,0,0")).
                setCoder(StringUtf8Coder.of());

        PCollection <String> output =
                input.apply("Emit <country, 1>", ParDo.of(new StarterPipeline.FormatCountry())).
                        apply("Group by country", Combine.perKey(Sum.ofIntegers())).
                        apply("Prepare output", ParDo.of(new StarterPipeline.FormatOutput()));

        PAssert.that(output).containsInAnyOrder("Mainland China,3");

        p.run().waitUntilFinish();
    }

}