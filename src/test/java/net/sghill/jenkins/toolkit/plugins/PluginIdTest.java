package net.sghill.jenkins.toolkit.plugins;

import net.sghill.jenkins.toolkit.parsing.HostedGitRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PluginIdTest {

    @Test
    void shouldTrimSuffix() {
        HostedGitRepository repository = new HostedGitRepository("github.com", "jenkinsci", "BlameSubversion-plugin");
        PluginId actual = PluginId.from(repository);
        assertThat(actual).isEqualTo(new PluginId("BlameSubversion"));
    }

    @Test
    void shouldHandleNoSuffix() {
        HostedGitRepository repository = new HostedGitRepository("github.com", "jenkinsci", "DotCi");
        PluginId actual = PluginId.from(repository);
        assertThat(actual).isEqualTo(new PluginId("DotCi"));
    }
}
