package org.opencds.cqf.fhir.cr.cli.parameter;

import java.util.List;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class RawParameter {
    @Option(names = { "-fv", "--fhir-version" }, required = true)
    public String fhirVersion;

    @Option(names = { "-op", "--options-path" })
    public String optionsPath;

    @ArgGroup(multiplicity = "0..1", exclusive = false)
    public NamespaceParameter namespace;

    @ArgGroup(multiplicity = "1..*", exclusive = false)
    public List<LibraryParameter> libraries;
}
