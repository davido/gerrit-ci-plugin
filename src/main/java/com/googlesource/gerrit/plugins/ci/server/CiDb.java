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

package com.googlesource.gerrit.plugins.ci.server;

import com.google.gerrit.reviewdb.server.SchemaVersionAccess;
import com.google.gwtorm.server.Relation;
import com.google.gwtorm.server.Schema;

public interface CiDb extends Schema {

  @Relation(id = 1)
  SchemaVersionAccess schemaVersion();

  @Relation(id = 2)
  PatchSetVerificationAccess patchSetVerifications();
}
