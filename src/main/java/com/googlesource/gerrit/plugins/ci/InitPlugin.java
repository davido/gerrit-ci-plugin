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

package com.googlesource.gerrit.plugins.ci;

import com.google.gerrit.pgm.init.api.InitStep;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.ci.server.CiDb;

public class InitPlugin implements InitStep {

  // TODO(davido): Add site initialization logic
  @SuppressWarnings("unused")
  private final CiDb ciDb;

  @Inject
  InitPlugin(CiDb ciDb) {
    this.ciDb = ciDb;
  }

  @Override
  public void run() throws Exception {
  }

  @Override
  public void postRun() throws Exception {
  }
}
