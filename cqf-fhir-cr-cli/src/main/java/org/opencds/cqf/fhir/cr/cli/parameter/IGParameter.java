package org.opencds.cqf.fhir.cr.cli.parameter;

import java.util.List;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class IGParameter {
    @Option(names = { "-ig", "--ig-path" })
    public String igPath;

    @ArgGroup(multiplicity = "1..*")
    List<LibraryParameter> libraries;

    @Option(names = { "-rd", "--root-dir" })
    public String rootDir;
}
