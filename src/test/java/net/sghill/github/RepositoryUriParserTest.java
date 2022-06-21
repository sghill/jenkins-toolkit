package net.sghill.github;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryUriParserTest {
    private final RepositoryUriParser parser = new RepositoryUriParser();

    @ParameterizedTest
    @MethodSource("uris")
    void shouldParse(String uri, HostedGitRepository expected) {
        HostedGitRepository actual = parser.parse(uri);
        assertThat(actual).isEqualTo(expected);
    }

    static Stream<Arguments> uris() {
        return Stream.of(
                Arguments.of("https://github.com/jenkinsci/promoted-builds-simple-plugin", new HostedGitRepository("github.com", "jenkinsci", "promoted-builds-simple-plugin")),
                Arguments.of("https://github.com/jenkinsci/BlameSubversion-plugin", new HostedGitRepository("github.com", "jenkinsci", "BlameSubversion-plugin"))
        );
    }
}
