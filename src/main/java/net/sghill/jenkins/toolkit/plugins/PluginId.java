package net.sghill.jenkins.toolkit.plugins;

import lombok.Value;
import net.sghill.jenkins.toolkit.parsing.HostedGitRepository;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Value
public class PluginId {
    String id;

    public static PluginId from(HostedGitRepository repository) {
        return Optional.ofNullable(repository)
                .map(HostedGitRepository::getName)
                .map(n -> StringUtils.removeEnd(n, "-plugin"))
                .map(PluginId::new)
                .orElse(null);
    }
}
