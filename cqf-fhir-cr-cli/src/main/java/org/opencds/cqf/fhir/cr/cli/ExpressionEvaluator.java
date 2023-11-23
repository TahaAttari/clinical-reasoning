package org.opencds.cqf.fhir.cr.cli;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.hl7.elm.r1.VersionedIdentifier;
import org.opencds.cqf.cql.engine.execution.CqlEngine;
import org.opencds.cqf.cql.engine.execution.Environment;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.opencds.cqf.fhir.cr.cli.parameter.CqlParameterGroup;
import org.opencds.cqf.fhir.cr.cli.parameter.EvaluationParameterGroup;
import org.opencds.cqf.fhir.cr.cli.parameter.LibraryParameter;

// TODO: Placeholder class for now. Really, this should be internal to the engine?
// How should we handle argument conversion? Do that ahead of time based on the ELM?
public class ExpressionEvaluator {

    public static List<EvaluationResult> evaluateExpressions(Environment environment,
            EvaluationParameterGroup evaluationParameters) {
        requireNonNull(environment);
        requireNonNull(evaluationParameters);

        var engine = new CqlEngine(environment);
        var results = new ArrayList<EvaluationResult>(evaluationParameters.libraries.size());

        for (LibraryParameter library : evaluationParameters.libraries) {
            var identifier = new VersionedIdentifier()
                    .withId(library.libraryName)
                    .withVersion(library.libraryVersion);

            var arguments = createArguments(
                    environment.getLibraryManager(),
                    identifier,
                    evaluationParameters.cqlParameter);

            // TODO: Bug in the CQL engine, doesn't correctly handle multiple context
            // arguments!
            Pair<String, Object> contextArgument = null;
            if (!arguments.contextArguments().isEmpty()) {
                var first = arguments.contextArguments().entrySet().iterator().next();
                contextArgument = Pair.of(first.getKey(), first.getValue());
            }

            var result = engine.evaluate(
                    identifier,
                    contextArgument,
                    arguments.libraryArguments());

            results.add(result);
        }

        return results;
    }

    private static CqlArguments createArguments(LibraryManager libraryManager, VersionedIdentifier libraryIdentifier,
            CqlParameterGroup cqlParameterGroup) {
        // TODO: Bug in the CQL engine. Doesn't correctly handle multiple context
        // arguments.
        var context = cqlParameterGroup.context;
        checkArgument(
                context.size() <= 1,
                "Multiple context arguments are not currently supported");

        // TODO: Correct handling of the input parameters means looking up the input
        // parameter in the library
        // and converting the incoming string to the correct type.
        var parameters = cqlParameterGroup.parameters;
        checkArgument(
                parameters == null || parameters.isEmpty(),
                "Library parameters are not currently supported");

        // TODO: The CQL engine needs some type like this. This is a stopgap for the
        // moment.
        var args = new CqlArguments();

        for (var c : context) {
            args.contextArguments().put(c.contextName, c.contextValue);
        }

        return args;
    }
}
