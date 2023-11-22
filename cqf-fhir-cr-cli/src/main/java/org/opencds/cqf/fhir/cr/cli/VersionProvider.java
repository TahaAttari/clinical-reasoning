package org.opencds.cqf.fhir.cr.cli;

import picocli.CommandLine.IVersionProvider;

public class VersionProvider implements IVersionProvider {

    @Override
    public String[] getVersion() throws Exception {
        String version = VersionProvider.class.getPackage().getImplementationVersion();
        String cqlVersion = org.opencds.cqf.cql.engine.execution.CqlEngine.class.getPackage()
                .getImplementationVersion();
        String cqlSpecificationVersion = org.opencds.cqf.cql.engine.execution.CqlEngine.class.getPackage()
                .getSpecificationVersion();
        return new String[] {
                "cqf-fhir-cr-cli version: " + version,
                "CQL version: " + cqlVersion,
                "CQL specification version: " + cqlSpecificationVersion,
                "Copyright 2019+ Smile Digital Health",
                "Apache License Version 2.0 <http://www.apache.org/licenses/>",
                "There is NO WARRANTY, to the extent permitted by law."
        };
    }
}
