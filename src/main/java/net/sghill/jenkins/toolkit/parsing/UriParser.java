package net.sghill.jenkins.toolkit.parsing;

public interface UriParser<T> {
    T parse(String uri);
}
