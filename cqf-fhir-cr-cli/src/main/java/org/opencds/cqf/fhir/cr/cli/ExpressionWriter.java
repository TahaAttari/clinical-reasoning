package org.opencds.cqf.fhir.cr.cli;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.opencds.cqf.cql.engine.execution.ExpressionResult;

// TODO: Need a story for converting / serializing results too. Hmmm.
public class ExpressionWriter {

    public static void writeResults(List<EvaluationResult> result, StringBuilder output) {
        requireNonNull(result);
        requireNonNull(output);

        for (var r : result) {
            writeResult(r, output);
        }
    }

    // TODO: Yeesh, need a story for this too.
    private static void writeResult(EvaluationResult result, StringBuilder output) {
        for (Map.Entry<String, ExpressionResult> libraryEntry : result.expressionResults.entrySet()) {
            output.append(libraryEntry.getKey() + "="
                    + tempConvert(libraryEntry.getValue().value()) + "\n");
        }
    }

    // TODO: This serialization logic needs to be some work..
    // We need a way to output full resources if needed.
    private static String tempConvert(Object value) {
        if (value == null) {
            return "null";
        }

        String result = "";
        if (value instanceof Iterable) {
            result += "[";
            Iterable<?> values = (Iterable<?>) value;
            for (Object o : values) {

                result += (tempConvert(o) + ", ");
            }

            if (result.length() > 1) {
                result = result.substring(0, result.length() - 2);
            }

            result += "]";
        } else if (value instanceof IBaseResource) {
            IBaseResource resource = (IBaseResource) value;
            result = resource.fhirType()
                    + (resource.getIdElement() != null
                            && resource.getIdElement().hasIdPart()
                                    ? "(id=" + resource.getIdElement().getIdPart() + ")"
                                    : "");
        } else if (value instanceof IBase) {
            result = ((IBase) value).fhirType();
        } else if (value instanceof IBaseDatatype) {
            result = ((IBaseDatatype) value).fhirType();
        } else {
            result = value.toString();
        }

        return result;
    }

}
