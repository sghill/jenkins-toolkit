package net.sghill.jenkins.toolkit.svgs;

import net.sghill.jenkins.toolkit.parsing.RepositoryUriParser;
import net.sghill.jenkins.toolkit.plugins.PluginId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.StringReader;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CsvParserTest {
    private final CsvParser parser = new CsvParser(new RepositoryUriParser());

    @Test
    void shouldAssignStateCorrectly() {
        Map<PluginId, OssState> actual = parser.parseFromClasspath();
        assertThat(actual)
                .containsEntry(new PluginId("coordinator"), OssState.ABANDONED)
                .containsEntry(new PluginId("selenium"), OssState.DEPRECATED)
                .containsEntry(new PluginId("jenkinslint"), OssState.MERGED)
                .containsEntry(new PluginId("computer-queue"), OssState.PROPOSED)
                .containsEntry(new PluginId("envinject"), OssState.UPDATED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"selenium", "\nselenium\n\n"})
    void shouldParseExpectedFormat(String input) {
        Set<PluginId> actual = parser.parseFrom(new StringReader(input));
        assertThat(actual).containsExactly(new PluginId("selenium"));
    }
}
