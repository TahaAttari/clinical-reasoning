package org.opencds.cqf.fhir.cr.cli.parameter;

import ca.uhn.fhir.rest.api.EncodingEnum;
import picocli.CommandLine.Option;

/**
 * The root directory and IG resource to use for IG context evaluation
 */
public class IGParameter {
    @Option(names = { "-ig",
            "--ig-path" }, required = true, description = "the path to the IG resource to use for evaluation.")
    public String igPath;

    @Option(names = { "-rd",
            "--root-dir" }, description = "the path of the root directory of the IG to use for evaluation.")
    public String rootDir;

    @Option(names = { "-enc",
            "--encoding" }, description = "the file format the IG uses (XML or JSON). Defaults to JSON.", defaultValue = "JSON")
    public EncodingEnum encodingEnum;
}
