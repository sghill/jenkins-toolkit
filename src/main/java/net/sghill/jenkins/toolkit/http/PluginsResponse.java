package net.sghill.jenkins.toolkit.http;

import lombok.Data;

import java.util.List;

@Data
public class PluginsResponse {
    private List<Plugin> plugins;
}
