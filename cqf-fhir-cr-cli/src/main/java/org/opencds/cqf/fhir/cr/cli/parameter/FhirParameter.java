package org.opencds.cqf.fhir.cr.cli.parameter;

import picocli.CommandLine.Option;

/**
 * Parameters related to FHIR
 */
public class FhirParameter {
    @Option(names = { "-fv", "--fhir-version" }, required = true)
    public String fhirVersion;
}
