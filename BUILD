load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "gerrit_plugin",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
)

gerrit_plugin(
    name = "gerrit-ci-plugin",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**/*"]),
    manifest_entries = [
      "Gerrit-PluginName: ci",
      "Gerrit-Module: com.googlesource.gerrit.plugins.ci.GlobalModule",
      "Gerrit-SshModule: com.googlesource.gerrit.plugins.ci.SshModule",
      "Gerrit-InitStep: com.googlesource.gerrit.plugins.ci.init.InitPlugin",
      "Implementation-Title: CI plugin",
      "Implementation-URL: https://github.com/davido/gerrit-ci-plugin/",
    ],
    provided_deps = [
        "//lib/commons:dbcp"
        "//lib:gson",
    ]
)

junit_tests(
    name = "gerrit_ci_plugin_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["gerrit-ci-plugin"],
    deps = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":gerrit-ci-plugin__plugin",
    ],
)
