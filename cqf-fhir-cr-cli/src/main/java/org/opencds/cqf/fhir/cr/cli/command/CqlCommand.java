package org.opencds.cqf.fhir.cr.cli.command;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;
import org.cqframework.cql.cql2elm.CqlCompilerOptions;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.CqlTranslatorOptionsMapper;
import org.cqframework.cql.cql2elm.DefaultLibrarySourceProvider;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.opencds.cqf.cql.engine.data.DataProvider;
import org.opencds.cqf.cql.engine.execution.Environment;
import org.opencds.cqf.cql.engine.terminology.TerminologyProvider;
import org.opencds.cqf.fhir.cr.cli.parameter.EvaluationParameterGroup;
import org.opencds.cqf.fhir.cr.cli.parameter.FhirParameter;
import org.opencds.cqf.fhir.utility.Constants;
import org.opencds.cqf.fhir.cr.cli.ExpressionEvaluator;
import org.opencds.cqf.fhir.cr.cli.ExpressionWriter;
import org.opencds.cqf.fhir.cr.cli.parameter.EnvironmentParameter;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Command(name = "cql", mixinStandardHelpOptions = true, description = "evaluate a set of CQL expressions contained within some CQL libraries")
public class CqlCommand implements Callable<Integer> {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CqlCommand.class);

    @ArgGroup(multiplicity = "1", exclusive = false)
    public FhirParameter fhirParameter;

    @ArgGroup(multiplicity = "1", exclusive = false)
    public EnvironmentParameter environment;

    @ArgGroup(multiplicity = "1", exclusive = false)
    public EvaluationParameterGroup evaluationParameters;

    @Override
    public Integer call() throws Exception {
        try {
            runRaw(environment);
            return 0;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return 1;
        }
    }

    private CqlTranslatorOptions loadTranslatorOptions(String optionsPath) {
        try {
            return CqlTranslatorOptionsMapper.fromFile(optionsPath);
        } catch (Exception e) {
            // intentionally ignored.
        }

        return null;
    }

    private Environment createEnvironment(FhirContext fhirContext, EnvironmentParameter environment) {
        var translatorOptions = loadTranslatorOptions(environment.optionsPath);
        var compilerOptions = translatorOptions != null ? translatorOptions.getCqlCompilerOptions()
                : CqlCompilerOptions.defaultOptions();

        var libraryManager = new LibraryManager(new ModelManager(), compilerOptions);
        var sourceProvider = new DefaultLibrarySourceProvider(Path.of(environment.libraryPath));

        libraryManager.getLibrarySourceLoader().registerProvider(sourceProvider);

        // TODO: Hmm... reuse the Repository provider here?
        DataProvider dataProvider = null;

        // TODO: Hmm... reuse the Repository terminolofy provider here?
        TerminologyProvider terminology = null;

        return new Environment(
                libraryManager,
                Map.of(Constants.FHIR_MODEL_URI, dataProvider),
                terminology);
    }

    private void runRaw(EnvironmentParameter environment) {
        requireNonNull(environment);

        var fhirVersionEnum = FhirVersionEnum.valueOf(fhirParameter.fhirVersion);
        var fhirContext = FhirContext.forCached(fhirVersionEnum);
        var e = createEnvironment(fhirContext, environment);

        var result = ExpressionEvaluator.evaluateExpressions(e, evaluationParameters);
        var sb = new StringBuilder();
        ExpressionWriter.writeResults(result, sb);
        System.out.println(sb.toString());
    }
}
