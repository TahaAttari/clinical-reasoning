package org.opencds.cqf.fhir.cr.plandefinition.r5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.EncodingEnum;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r5.model.Bundle.BundleType;
import org.hl7.fhir.r5.model.Enumerations.FHIRTypes;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Parameters;
import org.hl7.fhir.r5.model.Resource;
import org.json.JSONException;
import org.opencds.cqf.fhir.api.Repository;
import org.opencds.cqf.fhir.cql.EvaluationSettings;
import org.opencds.cqf.fhir.cql.LibraryEngine;
import org.opencds.cqf.fhir.cql.engine.retrieve.RetrieveSettings.SEARCH_FILTER_MODE;
import org.opencds.cqf.fhir.cql.engine.retrieve.RetrieveSettings.TERMINOLOGY_FILTER_MODE;
import org.opencds.cqf.fhir.cql.engine.terminology.TerminologySettings.VALUESET_EXPANSION_MODE;
import org.opencds.cqf.fhir.utility.repository.IGFileStructureRepository;
import org.opencds.cqf.fhir.utility.repository.IGLayoutMode;
import org.opencds.cqf.fhir.utility.repository.InMemoryFhirRepository;
import org.opencds.cqf.fhir.utility.repository.Repositories;
import org.skyscreamer.jsonassert.JSONAssert;

public class PlanDefinition {
    private static final FhirContext fhirContext = FhirContext.forCached(FhirVersionEnum.R5);
    private static final IParser jsonParser = fhirContext.newJsonParser().setPrettyPrint(true);
    private static final EvaluationSettings evaluationSettings = EvaluationSettings.getDefault();

    static {
        evaluationSettings
                .getRetrieveSettings()
                .setSearchParameterMode(SEARCH_FILTER_MODE.FILTER_IN_MEMORY)
                .setTerminologyParameterMode(TERMINOLOGY_FILTER_MODE.FILTER_IN_MEMORY);

        evaluationSettings
                .getTerminologySettings()
                .setValuesetExpansionMode(VALUESET_EXPANSION_MODE.PERFORM_NAIVE_EXPANSION);
    }

    private static InputStream open(String asset) {
        return PlanDefinition.class.getResourceAsStream(asset);
    }

    public static String load(InputStream asset) throws IOException {
        return new String(asset.readAllBytes(), StandardCharsets.UTF_8);
    }

    public static String load(String asset) throws IOException {
        return load(open(asset));
    }

    public static IBaseResource parse(String asset) {
        return jsonParser.parseResource(open(asset));
    }

    public static PlanDefinitionProcessor buildProcessor(Repository repository) {
        return new PlanDefinitionProcessor(repository, EvaluationSettings.getDefault());
    }

    /** Fluent interface starts here **/
    public static class Assert {
        public static Apply that(String planDefinitionID, String patientID, String encounterID) {
            return new Apply(planDefinitionID, patientID, encounterID);
        }
    }

    public static class Apply {
        private String planDefinitionID;

        private String patientID;
        private String encounterID;

        private Repository repository;
        private Repository dataRepository;
        private Repository contentRepository;
        private Repository terminologyRepository;
        private Bundle additionalData;
        private Parameters parameters;
        private final FhirContext fhirContext = FhirContext.forR5Cached();

        public Apply(String planDefinitionID, String patientID, String encounterID) {
            this.planDefinitionID = planDefinitionID;
            this.patientID = patientID;
            this.encounterID = encounterID;
        }

        public Apply withData(String dataAssetName) {
            dataRepository = new InMemoryFhirRepository(fhirContext, (Bundle) parse(dataAssetName));

            return this;
        }

        public Apply withContent(String dataAssetName) {
            contentRepository = new InMemoryFhirRepository(fhirContext, (Bundle) parse(dataAssetName));

            return this;
        }

        public Apply withTerminology(String dataAssetName) {
            terminologyRepository = new InMemoryFhirRepository(fhirContext, (Bundle) parse(dataAssetName));

            return this;
        }

        public Apply withAdditionalData(String dataAssetName) {
            var data = parse(dataAssetName);
            additionalData = data.getIdElement().getResourceType().equals(FHIRTypes.BUNDLE.toCode())
                    ? (Bundle) data
                    : new Bundle()
                            .setType(BundleType.COLLECTION)
                            .addEntry(new BundleEntryComponent().setResource((Resource) data));

            return this;
        }

        public Apply withParameters(Parameters params) {
            parameters = params;

            return this;
        }

        public Apply withRepository(Repository repository) {
            this.repository = repository;

            return this;
        }

        private void buildRepository() {
            if (repository != null) {
                return;
            }
            var local = new IGFileStructureRepository(
                    fhirContext,
                    this.getClass()
                                    .getProtectionDomain()
                                    .getCodeSource()
                                    .getLocation()
                                    .getPath() + "org/opencds/cqf/fhir/cr/plandefinition/r4",
                    IGLayoutMode.TYPE_PREFIX,
                    EncodingEnum.JSON);
            if (dataRepository == null) {
                dataRepository = local;
            }
            if (contentRepository == null) {
                contentRepository = local;
            }
            if (terminologyRepository == null) {
                terminologyRepository = local;
            }

            repository = Repositories.proxy(dataRepository, contentRepository, terminologyRepository);
        }

        public GeneratedBundle apply() {
            buildRepository();
            var libraryEngine = new LibraryEngine(this.repository, evaluationSettings);
            return new GeneratedBundle((Bundle) buildProcessor(repository)
                    .apply(
                            new IdType("PlanDefinition", planDefinitionID),
                            null,
                            null,
                            patientID,
                            encounterID,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            parameters,
                            null,
                            additionalData,
                            null,
                            libraryEngine));
        }
    }

    static class GeneratedBundle {
        Bundle bundle;

        public GeneratedBundle(Bundle bundle) {
            this.bundle = bundle;
        }

        public void hasContained(int theCount) {
            assertEquals(bundle.getEntry().size(), theCount);
        }

        public void isEqualsTo(String expectedBundleAssetName) {
            try {
                JSONAssert.assertEquals(load(expectedBundleAssetName), jsonParser.encodeResourceToString(bundle), true);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                fail("Unable to compare Jsons: " + e.getMessage());
            }
        }
    }
}
