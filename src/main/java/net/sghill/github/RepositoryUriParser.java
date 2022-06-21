package net.sghill.github;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class RepositoryUriParser implements UriParser<HostedGitRepository> {
    @Override
    public HostedGitRepository parse(String input) {
        try {
            URI uri = new URI(input);
            String host = uri.getHost();
            String path = uri.getPath();
            String org = path.startsWith("/") ? StringUtils.substringBetween(path, "/") : StringUtils.substringBefore(path, "/");
            String name = StringUtils.substringBefore(StringUtils.substringAfter(path, org + "/"), "/");
            return new HostedGitRepository(host, org, name);
        } catch (URISyntaxException e) {
            log.error("Failed to parse {}", input, e);
            return null;
        }
    }
}
