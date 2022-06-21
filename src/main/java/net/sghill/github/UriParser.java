package net.sghill.github;

public interface UriParser<T> {
    T parse(String uri);
}
