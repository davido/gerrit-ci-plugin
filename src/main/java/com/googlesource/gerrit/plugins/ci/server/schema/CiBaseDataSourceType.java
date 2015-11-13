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

import java.io.IOException;
import java.io.InputStream;

import com.googlesource.gerrit.plugins.ci.server.CiDb;

public abstract class CiBaseDataSourceType implements CiDataSourceType {

  private final String driver;

  protected CiBaseDataSourceType(String driver) {
    this.driver = driver;
  }

  @Override
  public final String getDriver() {
    return driver;
  }

  @Override
  public boolean usePool() {
    return true;
  }

  @Override
  public ScriptRunner getIndexScript() throws IOException {
    return getScriptRunner("index_generic.sql");
  }

  protected static final ScriptRunner getScriptRunner(String path) throws IOException {
    if (path == null) {
      return ScriptRunner.NOOP;
    }
    ScriptRunner runner;
    try (InputStream in = CiDb.class.getResourceAsStream(path)) {
      if (in == null) {
        throw new IllegalStateException("SQL script " + path + " not found");
      }
      runner = new ScriptRunner(path, in);
    }
    return runner;
  }
}
