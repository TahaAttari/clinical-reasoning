package org.opencds.cqf.fhir.cr.cli.command;

import org.opencds.cqf.fhir.cr.cli.version.VersionProvider;
import picocli.CommandLine.Command;

@Command(footer = "Copyright 2019+ Smile Digital Health", subcommands = { CqlCommand.class,
        ArgFileCommand.class }, mixinStandardHelpOptions = true, versionProvider = VersionProvider.class)
public class CliCommand {
}
