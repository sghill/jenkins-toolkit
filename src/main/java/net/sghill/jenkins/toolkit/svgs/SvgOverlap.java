package net.sghill.jenkins.toolkit.svgs;

import net.sghill.jenkins.toolkit.parsing.RepositoryUriParser;
import net.sghill.jenkins.toolkit.plugins.PluginId;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Command
public class SvgOverlap implements Callable<Integer> {
    private final CsvParser parser;
    private final Categorizer categorizer;

    @Option(names = {"-i", "--installed"}, description = "File with installed plugin ids", required = true)
    private Path installedPlugins;

    @Option(names = {"-m", "--managed"}, description = "File with managed plugin ids")
    private Path managedPlugins;

    @Option(names = {"-r", "--removed"}, description = "File with removed plugin ids")
    private Path removedPlugins;

    @Option(names = {"-o", "--out"}, description = "Output CSV", required = true)
    private Path output;

    SvgOverlap(CsvParser parser, Categorizer categorizer) {
        this.parser = parser;
        this.categorizer = categorizer;
    }

    @Override
    public Integer call() throws Exception {
        Map<PluginId, OssState> referenceData = parser.parseFromClasspath();
        Map<InternalState, Set<PluginId>> byInternalState = new HashMap<>();
        Set<PluginId> managed = from(managedPlugins);
        Set<PluginId> installed = from(installedPlugins).stream()
                .filter(p -> !managed.contains(p))
                .collect(Collectors.toSet());
        byInternalState.put(InternalState.UNMANAGED, installed);
        byInternalState.put(InternalState.MANAGED, managed);
        byInternalState.put(InternalState.REMOVED, from(removedPlugins));

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("pluginId", "oss", "internal", "result")
                .build();
        try (CSVPrinter printer = csvFormat.print(output, StandardCharsets.UTF_8)) {
            for (Map.Entry<InternalState, Set<PluginId>> entry : byInternalState.entrySet()) {
                InternalState internal = entry.getKey();
                for (PluginId pluginId : entry.getValue()) {
                    OssState oss = referenceData.get(pluginId);
                    Result result = categorizer.categorize(oss, internal);
                    printer.printRecord(pluginId.getId(), oss, internal, result);
                }
            }
        }

        return 0;
    }

    private Set<PluginId> from(Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            return Collections.emptySet();
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            return parser.parseFrom(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        SvgOverlap overlap = new SvgOverlap(new CsvParser(new RepositoryUriParser()), new Categorizer());
        int exitCode = new CommandLine(overlap).execute(args);
        System.exit(exitCode);
    }
}
