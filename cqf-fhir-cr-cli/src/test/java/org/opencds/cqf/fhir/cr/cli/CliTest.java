package org.opencds.cqf.fhir.cr.cli;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class CliTest extends BaseTest {

    @Test
    public void testVersion() {
        String[] args = new String[] { "-V" };
        Main.run(args);
        assertTrue(outContent.toString().startsWith("cqf-fhir-cr-cli version:"));
    }

    @Test
    public void testHelp() {
        String[] args = new String[] { "-h" };
        Main.run(args);
        String output = outContent.toString();
        assertTrue(output.startsWith("Usage:"));
        // assertTrue(output.endsWith("Patient=123\n"));
    }

    @Test
    public void testEmpty() {
        String[] args = new String[] {};
        Main.run(args);
        String output = errContent.toString();
        assertTrue(output.startsWith("Missing required subcommand"));
        // assertTrue(output.endsWith("Patient=123\n"));
    }

    @Test
    public void testNull() {
        assertThrows(NullPointerException.class, () -> {
            Main.run(null);
        });
    }

    @Test
    public void testArgFile() {
        String[] args = new String[] { "argfile", testResourcePath + "/argfile/args.txt" };

        Main.run(args);

        String output = outContent.toString();

        assertTrue(output.contains("Patient=Patient(id=example)"));
        assertTrue(output.contains("TestAdverseEvent=[AdverseEvent(id=example)]"));
    }
}
