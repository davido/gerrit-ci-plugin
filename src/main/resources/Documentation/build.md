Build
=====

This @PLUGIN@ plugin is built with Bazel.

Clone (or link) both this plugin to the `plugins` directory of Gerrit's source tree.


Then issue

```
  bazel build plugins/@PLUGIN@
```

in the root of Gerrit's source tree to build

The output is created in

```
  bazel-genfiles/plugins/@PLUGIN@/@PLUGIN@.jar
```

This project can be imported into the Eclipse IDE.
Add the plugin name to the `CUSTOM_PLUGINS` set in
Gerrit core in `tools/bzl/plugins.bzl`, and execute:

```
  ./tools/eclipse/project.py
```

To execute the tests run:

```
  bazel test plugins/@PLUGIN@
```

[Back to @PLUGIN@ documentation index][index]

[index]: index.html
