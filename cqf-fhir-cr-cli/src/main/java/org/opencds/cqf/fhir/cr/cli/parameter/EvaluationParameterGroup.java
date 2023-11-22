package org.opencds.cqf.fhir.cr.cli.parameter;

import java.util.List;

import picocli.CommandLine.ArgGroup;

/**
 * Parameters related to evaluation, which expressions to evaluate and what
 * parameters to use.
 */
public class EvaluationParameterGroup {
    @ArgGroup(multiplicity = "0..1", exclusive = false)
    public CqlParameterGroup cqlParameter;

    @ArgGroup(multiplicity = "1..*", exclusive = false)
    public List<LibraryParameter> libraries;
}