package net.sghill.github;

import lombok.Value;

@Value
public class HostedGitRepository {
    String host;
    String org;
    String name;
}
