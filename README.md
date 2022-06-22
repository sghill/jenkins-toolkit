# jenkins-toolkit
Misc tools for Jenkins

### SVG Overlap

These classes correspond to the Jenkins 2.346.1 [SVG icon migration][svg] from June 2022.

`net.sghill.jenkins.toolkit.svgs.SvgOverlap#main` is the entrypoint for generating a CSV to help prepare deployments
for the migration.

The output looks like this:

```csv
pluginId,oss,internal,result
shiningpanda,PROPOSED,UNMANAGED,ADOPT_OR_REMOVE
```

Reference data is included from the blog post.
An operator will need to bring a file with installed plugins. It can be a CSV, but the first column must be pluginId.

For example:

```csv
ant
gradle
matrix-project
```

Lists of managed and removed plugins can also be passed.

We have some plugins that are automatically upgraded to latest on rollout (managed) and some that are not (unmanaged).
This is useful for determining migration scope.
A managed plugin should be automatically upgraded, but an unmanaged plugin will require additional work.

Removed plugins are useful for proving out how much past effort has helped with the current migration.
For example, 14 of the plugins that would have required an update here were already removed.
This further adds to the return on investment from prioritizing this tech debt removal work.


[svg]: https://www.jenkins.io/blog/2022/06/20/svg-icon-migration/
