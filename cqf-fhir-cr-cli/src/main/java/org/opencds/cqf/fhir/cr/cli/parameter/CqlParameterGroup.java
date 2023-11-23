package org.opencds.cqf.fhir.cr.cli.parameter;

import java.util.List;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

/*
 * Group of parameters related to CQL evaluation
 */
public class CqlParameterGroup {
    @ArgGroup(multiplicity = "0..*", exclusive = false)
    public List<InputParameter> parameters;

    @ArgGroup(multiplicity = "0..1", exclusive = false)
    public List<ContextParameter> context;

    /**
     * A set of context parameters for CQL (e.g. Patient=123, Encounter=ABC)
     */
    public static class ContextParameter {
        @Option(names = { "-c",
                "--context" }, description = "the context name (e.g. Patient, Encounter, etc.)", required = true)
        public String contextName;

        @Option(names = { "-cv",
                "--context-value" }, description = "the initial value for the context", required = true)
        public String contextValue;
    }

    /**
     * A set of CQL library input parameters to use for evaluation (e.g.
     * "Measurement Period")
     */
    public static class InputParameter {
        @Option(names = { "-p",
                "--parameter" }, description = "the name of the CQL parameter to set the value for (e.g. Measurement Period)", required = true)
        public String parameterName;

        @Option(names = { "-pv",
                "--parameter-value" }, description = "the value of the parameter (e.g. [@2019, @2020])", required = true)
        public String parameterValue;
    }
}
