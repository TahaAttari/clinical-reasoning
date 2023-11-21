package org.opencds.cqf.fhir.cr.cli.command;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.EncodingEnum;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.tuple.Pair;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.CqlTranslatorOptionsMapper;
import org.cqframework.fhir.utilities.IGContext;
import org.hl7.elm.r1.VersionedIdentifier;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.opencds.cqf.cql.engine.execution.CqlEngine;
import org.opencds.cqf.cql.engine.execution.EvaluationResult;
import org.opencds.cqf.cql.engine.execution.ExpressionResult;
import org.opencds.cqf.fhir.api.Repository;
import org.opencds.cqf.fhir.cql.CqlOptions;
import org.opencds.cqf.fhir.cql.EvaluationSettings;
import org.opencds.cqf.fhir.cr.cli.parameter.IGParameter;
import org.opencds.cqf.fhir.cr.cli.parameter.LibraryParameter;
import org.opencds.cqf.fhir.cr.cli.parameter.RawParameter;
import org.opencds.cqf.fhir.utility.repository.IGFileStructureRepository;
import org.opencds.cqf.fhir.utility.repository.IGLayoutMode;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Command(name = "cql", mixinStandardHelpOptions = true)
public class CqlCommand implements Callable<Integer> {

    @ArgGroup(multiplicity = "1")
    public RawParameter raw;

    @ArgGroup(multiplicity = "1")
    public IGParameter ig;

    private class Logger implements IWorkerContext.ILoggingService {

        private final org.slf4j.Logger log = LoggerFactory.getLogger(Logger.class);

        @Override
        public void logMessage(String s) {
            log.warn(s);
        }

        @Override
        public void logDebugMessage(LogCategory logCategory, String s) {
            log.debug("{}: {}", logCategory, s);
        }

        @Override
        public boolean isDebugLogging() {
            return log.isDebugEnabled();
        }
    }

    private Integer runRaw(RawParameter raw) {
        FhirVersionEnum fhirVersionEnum = FhirVersionEnum.valueOf(raw.fhirVersion);
        CqlOptions cqlOptions = CqlOptions.defaultOptions();

        if (raw.optionsPath != null) {
            CqlTranslatorOptions options = CqlTranslatorOptionsMapper.fromFile(raw.optionsPath);
            cqlOptions.setCqlCompilerOptions(options.getCqlCompilerOptions());
        }

        var evaluationSettings = EvaluationSettings.getDefault();
        evaluationSettings.setCqlOptions(cqlOptions);

        var engine = new CqlEngine(null);

        for (LibraryParameter library : raw.libraries) {

            VersionedIdentifier identifier = new VersionedIdentifier().withId(library.libraryName);

            Pair<String, Object> contextParameter = null;

            if (library.context != null) {
                contextParameter = Pair.of(library.context.contextName, library.context.contextValue);
            }

            EvaluationResult result = engine.evaluate(identifier, contextParameter);

            writeResult(result);
        }

        return 0;
    }

    private Integer runIg(IGParameter ig) {
        IGContext igContext = null;
        FhirVersionEnum fhirVersionEnum = FhirVersionEnum.valueOf(raw.fhirVersion);
        if (ig.rootDir != null && ig.igPath != null) {
            igContext = new IGContext(new Logger());
            igContext.initializeFromIg(ig.rootDir, ig.igPath, fhirVersionEnum.getFhirVersionString());
        }

        return 0;
    }

    @Override
    public Integer call() throws Exception {
        // JP - TODO - the real difference between raw and ig is the environment setup.
        // Then once we have the engine, we can call evaluate on it with the appropriate
        // parameters.
        if (raw != null) {
            return runRaw(raw);
        }

        return runIg(ig);
    }

    private Repository createRepository(FhirContext fhirContext, String rootDir) {
        if (rootDir == null) {
            return new NoOpRepository(fhirContext);
        }

        return new IGFileStructureRepository(fhirContext, rootDir, IGLayoutMode.DIRECTORY, EncodingEnum.JSON);
    }

    @SuppressWarnings("java:S106") // We are intending to output to the console here as a CLI tool
    private void writeResult(EvaluationResult result) {
        for (Map.Entry<String, ExpressionResult> libraryEntry : result.expressionResults.entrySet()) {
            System.out.println(libraryEntry.getKey() + "="
                    + this.tempConvert(libraryEntry.getValue().value()));
        }

        System.out.println();
    }

    private String tempConvert(Object value) {
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
