package org.opencds.cqf.fhir.cr.questionnaireresponse.dstu3;

import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.uhn.fhir.context.FhirContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opencds.cqf.fhir.cr.questionnaireresponse.TestQuestionnaireResponse.Given;
import org.opencds.cqf.fhir.utility.Ids;

public class QuestionnaireResponseProcessorTests {
    private final FhirContext fhirContext = FhirContext.forDstu3Cached();

    private void testExtract(String questionnaireResponse) {
        new Given()
                .repositoryFor(fhirContext, "dstu3")
                .when()
                .questionnaireResponseId(questionnaireResponse)
                .extract()
                .isEqualsToExpected(Ids.newId(fhirContext, "Bundle", "extract-" + questionnaireResponse));
    }

    @Test
    void test() {
        testExtract("QRSharonDecision");
    }

    @Test
    void testExtract_noQuestionnaireReference_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            testExtract("mypain-no-url");
        });
    }

    @Test
    void testIsSubjectExtension() {
        testExtract("sdc-profile-example-multi-subject");
    }

    @Test
    void testDefinitionBasedExtraction() {
        testExtract("OutpatientPriorAuthorizationRequest");
    }

    @Test
    @Disabled("JP - Don't know why this one is disabled")
    void testDemographicsExtraction() {
        testExtract("demographics-qr");
    }
}
