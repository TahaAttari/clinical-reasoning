package org.opencds.cqf.fhir.cr.cli.parameter;

import java.util.List;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class LibraryParameter {
    @Option(names = { "-ln", "--library-name" }, required = true)
    public String libraryName;

    @Option(names = { "-lv", "--library-version" })
    public String libraryVersion;

    @ArgGroup(multiplicity = "0..*", exclusive = false)
    public List<InputParameter> parameters;

    @Option(names = { "-e", "--expression" })
    public String[] expression;

    @ArgGroup(multiplicity = "0..1", exclusive = false)
    public ContextParameter context;
}
