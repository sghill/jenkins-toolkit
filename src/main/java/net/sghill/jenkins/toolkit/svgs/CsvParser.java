package net.sghill.jenkins.toolkit.svgs;

import lombok.extern.slf4j.Slf4j;
import net.sghill.jenkins.toolkit.parsing.HostedGitRepository;
import net.sghill.jenkins.toolkit.parsing.UriParser;
import net.sghill.jenkins.toolkit.plugins.PluginId;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class CsvParser {
    private final UriParser<HostedGitRepository> uriParser;

    public CsvParser(UriParser<HostedGitRepository> uriParser) {
        this.uriParser = uriParser;
    }

    public Map<PluginId, OssState> parseFromClasspath() {
        Map<PluginId, OssState> byId = new HashMap<>();
        for (OssState state : OssState.values()) {
            String path = String.format("/JENKINS-68251/%s.csv", state.name().toLowerCase());
            try (InputStream is = CsvParser.class.getResourceAsStream(path)) {
                assert is != null;
                try (InputStreamReader isr = new InputStreamReader(is);
                     CSVParser parser = CSVFormat.DEFAULT.parse(isr)) {
                    for (CSVRecord record : parser) {
                        String uri = record.get(0);
                        HostedGitRepository repository = uriParser.parse(uri);
                        PluginId id = PluginId.from(repository);
                        OssState existing = byId.get(id);
                        if (existing != null && existing != state) {
                            log.warn("{} already recorded as {} - replacing with {}", id, existing, state);
                        }
                        byId.put(id, state);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return byId;
    }

    public Set<PluginId> parseFrom(Reader reader) {
        Set<PluginId> ids = new HashSet<>();
        try (CSVParser records = CSVFormat.DEFAULT.parse(reader)) {
            for (CSVRecord record : records) {
                String pluginId = record.get(0);
                ids.add(new PluginId(pluginId));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ids;
    }
}
