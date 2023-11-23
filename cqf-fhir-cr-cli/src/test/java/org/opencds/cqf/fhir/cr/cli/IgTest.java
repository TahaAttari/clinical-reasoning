package org.opencds.cqf.fhir.cr.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import org.junit.jupiter.api.Test;

@TestInstance(Lifecycle.PER_CLASS)
public class IgTest extends BaseTest {

    @Test
    public void testR4() {
        String[] args = new String[] {
                "ig",
                "-fv=R4",
                "-ln=TestFHIR",
                "-rd=" + testResourcePath + "/r4",
                "-c=Patient",
                "-cv=example"
        };

        Main.run(args);

        String output = outContent.toString();

        String error = errContent.toString();

        assertTrue(output.contains("Patient=Patient(id=example)"));
        assertTrue(output.contains("TestAdverseEvent=[AdverseEvent(id=example)]"));
        assertTrue(output.contains("TestPatientGender=Patient(id=example)"));
        assertTrue(output.contains("TestPatientActive=Patient(id=example)"));
        assertTrue(output.contains("TestPatientBirthDate=Patient(id=example)"));
        assertTrue(output.contains("TestPatientMaritalStatusMembership=Patient(id=example)"));
        assertTrue(output.contains("TestPatientMartialStatusComparison=Patient(id=example)"));
        assertTrue(output.contains("TestPatientDeceasedAsBoolean=Patient(id=example)"));
        assertTrue(output.contains("TestPatientDeceasedAsDateTime=null"));
        assertTrue(output.contains("TestSlices=[Observation(id=blood-pressure)]"));
        assertTrue(output.contains("TestSimpleExtensions=Patient(id=example)"));
        assertTrue(output.contains("TestComplexExtensions=Patient(id=example)"));
    }

}
