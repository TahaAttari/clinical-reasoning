package org.opencds.cqf.fhir.cr.cli.parameter;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

/**
 * Parameters related to the environment (data, terminology, etc.) in the
 * absence of an IG
 */
public class EnvironmentParameter {
    @Option(names = { "-dp", "--data-path" }, description = {
            "relative path to the directory containing the resources to use for data. Defaults to no data." })
    public String dataPath;

    @Option(names = { "-lp", "--library-path" }, description = {
            "relative path to the directory containing the CQL libraries. Defaults to current directory." }, defaultValue = ".")
    public String libraryPath;

    @Option(names = { "-op", "--options-path" }, description = {
            "relative path to the directory containing the CQL compiler options. Defaults to current directory." }, defaultValue = ".")
    public String optionsPath;

    @Option(names = { "-tp", "--terminology-path" }, description = {
            "relative path to the directory containing the resource to use for terminology. Defaults to no terminology." })
    public String terminologyPath;

    @ArgGroup(multiplicity = "0..1", exclusive = false)
    public NamespaceParameter namespace;

    /**
     * The set of namespaces to configure for the evaluation
     */
    public static class NamespaceParameter {
        @Option(names = { "-nn", "--namespace-name" }, description = "the namespace to set a url for")
        public String namespaceName;

        @Option(names = { "-nu", "--namespace-uri" }, description = "the uri to set for the namespace")
        public String namespaceUri;
    }
}
