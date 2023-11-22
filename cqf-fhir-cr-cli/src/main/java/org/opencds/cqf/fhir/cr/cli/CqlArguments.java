package org.opencds.cqf.fhir.cr.cli;

import java.util.HashMap;
import java.util.Map;

// TODO: We need need to add such a class to the CQL engine API.
public class CqlArguments {

    private final Map<String, Object> contextArguments = new HashMap<>();
    private final Map<String, Object> libraryArguments = new HashMap<>();

    public Map<String, Object> contextArguments() {
        return contextArguments;
    }

    public Map<String, Object> libraryArguments() {
        return libraryArguments;
    }
}
