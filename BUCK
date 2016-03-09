include_defs('//bucklets/gerrit_plugin.bucklet')

gerrit_plugin(
  name = 'gerrit-ci-plugin',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/**/*']),
  manifest_entries = [
    'Gerrit-PluginName: ci',
    'Gerrit-Module: com.googlesource.gerrit.plugins.ci.GlobalModule',
    'Gerrit-SshModule: com.googlesource.gerrit.plugins.ci.SshModule',
    'Gerrit-InitStep: com.googlesource.gerrit.plugins.ci.init.InitPlugin',
    'Implementation-Title: CI plugin',
    'Implementation-URL: https://gerrit-review.googlesource.com/#/admin/projects/plugins/ci',
  ],
  provided_deps = [
    '//lib/commons:dbcp',
    '//lib:gson',
  ]
)

java_test(
  name = 'ci_tests',
  srcs = glob(['src/test/java/**/*IT.java']),
  labels = ['gerrit-ci-plugin'],
  source_under_test = [':gerrit-ci-plugin__plugin'],
  deps = GERRIT_PLUGIN_API + GERRIT_TESTS + [
    ':gerrit-ci-plugin__plugin',
  ],
)

java_library(
  name = 'classpath',
  deps = GERRIT_PLUGIN_API + GERRIT_TESTS + [
    ':gerrit-ci-plugin__plugin'
  ],
)
