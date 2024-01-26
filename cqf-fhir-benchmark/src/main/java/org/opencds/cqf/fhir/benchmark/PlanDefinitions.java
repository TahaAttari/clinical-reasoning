package org.opencds.cqf.fhir.benchmark;

import static org.opencds.cqf.fhir.utility.r4.Parameters.parameters;
import static org.opencds.cqf.fhir.utility.r4.Parameters.part;

import ca.uhn.fhir.context.FhirContext;
import java.util.concurrent.TimeUnit;
import org.hl7.fhir.r4.model.IdType;
import org.opencds.cqf.fhir.api.Repository;
import org.opencds.cqf.fhir.cr.plandefinition.r4.PlanDefinition;
import org.opencds.cqf.fhir.cr.plandefinition.r4.PlanDefinition.Apply;
import org.opencds.cqf.fhir.test.TestRepositoryFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
public class PlanDefinitions {
    private static final FhirContext FHIR_CONTEXT = FhirContext.forR4Cached();
    private static final Repository REPOSITORY = TestRepositoryFactory.createRepository(
            FHIR_CONTEXT, PlanDefinition.Assert.class, PlanDefinition.CLASS_PATH + "/anc-dak");

    private Apply apply;

    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        this.apply = PlanDefinition.Assert.that(
                        "ANCDT17",
                        "Patient/5946f880-b197-400b-9caa-a3c661d23041",
                        "Encounter/helloworld-patient-1-encounter-1",
                        null)
                .withRepository(REPOSITORY)
                .withParameters(parameters(part("encounter", "helloworld-patient-1-encounter-1")))
                .withExpectedBundleId(new IdType("Bundle", "ANCDT17"));
    }

    @Benchmark
    @Fork(warmups = 1, value = 1)
    @Measurement(iterations = 2, timeUnit = TimeUnit.SECONDS)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void test(Blackhole bh) throws Exception {
        // The Blackhole ensures that the compiler doesn't optimize
        // away this call, which does nothing with the result of the evaluation
        bh.consume(this.apply.applyR5());
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(PlanDefinitions.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
