package net.sghill.jenkins.toolkit.parsing;

import lombok.Value;

@Value
public class HostedGitRepository {
    String host;
    String org;
    String name;
}
