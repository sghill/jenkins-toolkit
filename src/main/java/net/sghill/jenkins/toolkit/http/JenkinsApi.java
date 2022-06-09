package net.sghill.jenkins.toolkit.http;

import retrofit2.Call;
import retrofit2.http.GET;

public interface JenkinsApi {
    @GET("/pluginManager/api/json?tree=plugins[shortName,requiredCoreVersion,version,hasUpdate]")
    Call<PluginsResponse> fetchPluginsWithRequiredCore();
}
