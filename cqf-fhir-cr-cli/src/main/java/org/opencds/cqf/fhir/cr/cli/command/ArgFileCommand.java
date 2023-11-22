package org.opencds.cqf.fhir.cr.cli.command;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import org.opencds.cqf.fhir.cr.cli.Main;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "argfile", description = "Use a text file as the source of command line arguments. Useful when the arguments are too long to fit on the command line or you need to save a set of arguments for reuse")
public class ArgFileCommand implements Callable<Integer> {

    @Parameters(arity = "1", description = "file containing arguments")
    File[] files;

    @Override
    public Integer call() throws Exception {
        var args = Files.readAllLines(files[0].toPath());
        return Main.run(args.toArray(String[]::new));
    }
}
