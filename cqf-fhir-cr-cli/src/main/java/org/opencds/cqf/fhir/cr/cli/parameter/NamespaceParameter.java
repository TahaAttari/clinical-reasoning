package org.opencds.cqf.fhir.cr.cli.parameter;

import picocli.CommandLine.Option;

public class NamespaceParameter {
    @Option(names = { "-nn", "--namespace-name" })
    public String namespaceName;

    @Option(names = { "-nu", "--namespace-uri" })
    public String namespaceUri;
}