package org.opencds.cqf.fhir.cr.cli.parameter;

import picocli.CommandLine.Option;

public class ContextParameter {
    @Option(names = { "-c", "--context" })
    public String contextName;

    @Option(names = { "-cv", "--context-value" })
    public String contextValue;
}
