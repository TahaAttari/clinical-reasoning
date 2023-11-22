package org.opencds.cqf.fhir.cr.cli.command;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.concurrent.Callable;

import org.cqframework.cql.cql2elm.CqlCompilerOptions;
import org.cqframework.cql.cql2elm.CqlTranslatorOptions;
import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.cqframework.fhir.npm.NpmLibrarySourceProvider;
import org.cqframework.fhir.npm.NpmModelInfoProvider;
import org.cqframework.fhir.npm.NpmProcessor;
import org.cqframework.fhir.utilities.IGContext;
import org.cqframework.fhir.utilities.LoggerAdapter;
import org.opencds.cqf.cql.engine.data.CompositeDataProvider;
import org.opencds.cqf.cql.engine.execution.Environment;
import org.opencds.cqf.fhir.api.Repository;
import org.opencds.cqf.fhir.cql.cql2elm.content.RepositoryFhirLibrarySourceProvider;
import org.opencds.cqf.fhir.cql.cql2elm.util.LibraryVersionSelector;
import org.opencds.cqf.fhir.cql.engine.model.FhirModelResolverCache;
import org.opencds.cqf.fhir.cql.engine.retrieve.RepositoryRetrieveProvider;
import org.opencds.cqf.fhir.cql.engine.retrieve.RetrieveSettings;
import org.opencds.cqf.fhir.cql.engine.retrieve.RetrieveSettings.PROFILE_MODE;
import org.opencds.cqf.fhir.cql.engine.retrieve.RetrieveSettings.SEARCH_FILTER_MODE;
import org.opencds.cqf.fhir.cql.engine.retrieve.RetrieveSettings.TERMINOLOGY_FILTER_MODE;
import org.opencds.cqf.fhir.cql.engine.terminology.RepositoryTerminologyProvider;
import org.opencds.cqf.fhir.cr.cli.ExpressionEvaluator;
import org.opencds.cqf.fhir.cr.cli.ExpressionWriter;
import org.opencds.cqf.fhir.cr.cli.parameter.EvaluationParameterGroup;
import org.opencds.cqf.fhir.cr.cli.parameter.FhirParameter;
import org.opencds.cqf.fhir.cr.cli.parameter.IGParameter;
import org.opencds.cqf.fhir.utility.Constants;
import org.opencds.cqf.fhir.utility.adapter.AdapterFactory;
import org.opencds.cqf.fhir.utility.repository.IGFileStructureRepository;
import org.opencds.cqf.fhir.utility.repository.IGLayoutMode;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.api.EncodingEnum;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;

@Command(name = "ig", mixinStandardHelpOptions = true, description = "evaluate a set of CQL expressions contained within the context of an IG")
public class IGCommand implements Callable<Integer> {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(IGCommand.class);

    @ArgGroup(multiplicity = "1", exclusive = false)
    public FhirParameter fhirParameter;

    @ArgGroup(multiplicity = "1")
    public IGParameter ig;

    @ArgGroup(multiplicity = "1", exclusive = false)
    public EvaluationParameterGroup evaluationParameters;

    @Override
    public Integer call() throws Exception {
        try {
            runIg(ig);
            return 0;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return 1;
        }
    }

    private CqlTranslatorOptions loadTranslatorOptions(String rootDir) {
        // TODO: Load compiler options from the IG...
        // There are two possible locations due to the varying IG conventions over time.
        // input/cql/cql-options.json is the typical place.
        // If that file exists, load it and return it the values.
        // CqlTranslatorOptions options =
        // CqlTranslatorOptionsMapper.fromFile(raw.optionsPath);
        // cqlOptions.setCqlCompilerOptions(options.getCqlCompilerOptions());
        return null;
    }

    private Environment createEnvironment(FhirContext fhirContext, String rootDir, EncodingEnum encodingEnum) {
        Repository repository = createRepository(fhirContext, rootDir, encodingEnum);

        var terminology = new RepositoryTerminologyProvider(repository);
        var dataProvider = new CompositeDataProvider(
                FhirModelResolverCache.resolverForVersion(fhirContext.getVersion().getVersion()),
                new RepositoryRetrieveProvider(repository, terminology,
                        new RetrieveSettings()
                                .setProfileMode(PROFILE_MODE.OFF)
                                .setSearchParameterMode(SEARCH_FILTER_MODE.FILTER_IN_MEMORY)
                                .setTerminologyParameterMode(TERMINOLOGY_FILTER_MODE.FILTER_IN_MEMORY)));

        var translatorOptions = loadTranslatorOptions(rootDir);
        var compilerOptions = translatorOptions != null ? translatorOptions.getCqlCompilerOptions()
                : CqlCompilerOptions.defaultOptions();

        var libraryManager = new LibraryManager(new ModelManager(), compilerOptions);

        AdapterFactory adapterFactory = getAdapterFactory(repository.fhirContext());

        var sourceProvider = new RepositoryFhirLibrarySourceProvider(
                repository, adapterFactory, new LibraryVersionSelector(adapterFactory));

        libraryManager.getLibrarySourceLoader().registerProvider(sourceProvider);

        return new Environment(
                libraryManager,
                Map.of(Constants.FHIR_MODEL_URI, dataProvider),
                terminology);
    }

    private void registerIgContext(Environment environment, FhirContext fhirContext, String rootDir, String igPath) {
        requireNonNull(environment);
        requireNonNull(rootDir);
        requireNonNull(fhirContext);
        requireNonNull(igPath);

        var logAdapter = new LoggerAdapter(logger);

        var igContext = new IGContext(logAdapter);
        igContext.initializeFromIg(rootDir, igPath,
                fhirContext.getVersion().getVersion().getFhirVersionString());
        var npmProcessor = new NpmProcessor(igContext);

        // Should never happen, sanity check
        requireNonNull(npmProcessor.getIgContext());
        requireNonNull(npmProcessor.getPackageManager());

        var reader = new org.cqframework.fhir.npm.LibraryLoader(
                npmProcessor.getIgContext().getFhirVersion());

        environment
                .getLibraryManager()
                .getModelManager()
                .getModelInfoLoader()
                .registerModelInfoProvider(new NpmModelInfoProvider(
                        npmProcessor.getPackageManager().getNpmList(), reader, logAdapter));

        environment
                .getLibraryManager()
                .getLibrarySourceLoader()
                .registerProvider(new NpmLibrarySourceProvider(
                        npmProcessor.getPackageManager().getNpmList(), reader, logAdapter));
    }

    public static AdapterFactory getAdapterFactory(FhirContext fhirContext) {
        switch (fhirContext.getVersion().getVersion()) {
            case DSTU3:
                return new org.opencds.cqf.fhir.utility.adapter.dstu3.AdapterFactory();
            case R4:
                return new org.opencds.cqf.fhir.utility.adapter.r4.AdapterFactory();
            case R5:
                return new org.opencds.cqf.fhir.utility.adapter.r5.AdapterFactory();
            default:
                throw new IllegalArgumentException(String.format("unsupported FHIR version: %s", fhirContext));
        }
    }

    private Repository createRepository(FhirContext fhirContext, String rootDir, EncodingEnum encodingEnum) {
        return new IGFileStructureRepository(fhirContext, rootDir, IGLayoutMode.DIRECTORY, EncodingEnum.JSON);
    }

    private void runIg(IGParameter ig) {
        requireNonNull(ig);

        var fhirVersionEnum = FhirVersionEnum.valueOf(fhirParameter.fhirVersion);
        var fhirContext = FhirContext.forCached(fhirVersionEnum);
        var environment = createEnvironment(fhirContext, ig.rootDir, ig.encodingEnum);

        if (ig.igPath != null) {
            registerIgContext(environment, fhirContext, ig.rootDir, ig.igPath);
        }

        var result = ExpressionEvaluator.evaluateExpressions(environment, evaluationParameters);
        var sb = new StringBuilder();
        ExpressionWriter.writeResults(result, sb);
        System.out.println(sb.toString());
    }
}
