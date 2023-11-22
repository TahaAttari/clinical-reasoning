package org.opencds.cqf.fhir.cr.cli.parameter;

import picocli.CommandLine.Option;

/**
 * The set of Libraries (and which expressions) to return results for
 */
public class LibraryParameter {
    @Option(names = { "-ln",
            "--library-name" }, required = true, description = "the name of the CQL library to evaluate")
    public String libraryName;

    @Option(names = { "-lv",
            "--library-version" }, description = "the version of the CQL library to use for evaluation. Defaults to latest. Errors if the specified version is not available.")
    public String libraryVersion;

    @Option(names = { "-e",
            "--expression" }, description = "the expressions of the CQL library to evaluation. Defaults to all.")
    public String[] expression;
}
