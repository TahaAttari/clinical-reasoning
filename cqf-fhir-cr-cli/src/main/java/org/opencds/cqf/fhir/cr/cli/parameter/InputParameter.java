package org.opencds.cqf.fhir.cr.cli.parameter;

public class InputParameter {
    @Option(names = { "-p", "--parameter" })
    public String parameterName;

    @Option(names = { "-pv", "--parameter-value" })
    public String parameterValue;
}