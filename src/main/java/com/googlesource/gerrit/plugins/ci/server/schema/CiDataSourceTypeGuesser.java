// Copyright (C) 2015 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.ci.server.schema;

import java.nio.file.Path;

import org.eclipse.jgit.lib.Config;

import com.google.common.base.Strings;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.config.SitePaths;
import com.google.inject.Inject;
import com.google.inject.ProvisionException;

public class CiDataSourceTypeGuesser {

  private final Config cfg;
  private final String configFile;

  @Inject
  CiDataSourceTypeGuesser(SitePaths site,
      PluginConfigFactory pluginConfig,
      @PluginName String pluginName) {
    this.cfg = pluginConfig.getGlobalPluginConfig(pluginName);
    configFile = String.format("%s.config", pluginName);
    Path config = site.resolve("etc").resolve(configFile);
    if (!config.toFile().exists()) {
      throw new ProvisionException(
          String.format("Config file %s for plugin %s doesn't exist",
              configFile, pluginName));
    }
  }

  public String guessDataSourceType() {
    String dbType = cfg.getString("database", null, "type");
    if (Strings.isNullOrEmpty(dbType)) {
      throw new ProvisionException(
          String.format("'database.type' must be defined in: %s", configFile));
    }
    return dbType.toLowerCase();
  }
}
