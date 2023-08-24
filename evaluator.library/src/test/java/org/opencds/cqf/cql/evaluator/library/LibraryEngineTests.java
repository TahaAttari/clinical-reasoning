package org.opencds.cqf.cql.evaluator.library;

import static org.opencds.cqf.cql.evaluator.fhir.util.r4.Parameters.parameters;
import static org.opencds.cqf.cql.evaluator.fhir.util.r4.Parameters.part;
import static org.testng.Assert.assertTrue;

import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.StringType;
import org.opencds.cqf.cql.evaluator.fhir.test.TestRepositoryFactory;
import org.testng.annotations.Test;

import ca.uhn.fhir.context.FhirContext;

public class LibraryEngineTests {

  @Test
  public void testFhirPath() {
    var patientId = "Patient/Patient1";
    var repository =
        TestRepositoryFactory.createRepository(FhirContext.forR4Cached(), this.getClass());
    var libraryEngine = new LibraryEngine(repository, EvaluationSettings.getDefault());

    var params = parameters();
    params.addParameter(part("%subject", new Patient().addName(new HumanName().addGiven("Alice"))));
    params.addParameter(
        part("%practitioner", new Practitioner().addName(new HumanName().addGiven("Michael"))));
    var expression = new CqfExpression("text/fhirpath",
        "'Greeting: Hello! ' + %subject.name.given.first() + ' Message: Test message Practitioner: ' + %practitioner.name.given.first()",
        null);

    var result = libraryEngine.resolveExpression(patientId, expression, params, null);
    assertTrue(((StringType) result.get(0)).getValue().equals(
        "Greeting: Hello! Alice Message: Test message Practitioner: Michael"));

    var expression2 = new CqfExpression("text/fhirpath",
        "'Provide discharge instructions for ' + %subject.name.given.first()", null);
    var result2 = libraryEngine.resolveExpression(patientId, expression2, params, null);
    assertTrue(((StringType) result2.get(0)).getValue().equals(
        "Provide discharge instructions for Alice"));
  }
}
