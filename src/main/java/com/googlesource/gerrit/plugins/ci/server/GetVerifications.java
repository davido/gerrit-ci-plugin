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

import com.google.common.collect.Maps;
import com.google.gerrit.extensions.restapi.RestReadView;
import com.google.gerrit.server.change.RevisionResource;
import com.google.gwtorm.server.OrmException;
import com.google.gwtorm.server.SchemaFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.ci.common.VerificationInfo;

import java.io.IOException;
import java.util.Map;

@Singleton
public class GetVerifications implements RestReadView<RevisionResource> {
  private final SchemaFactory<CiDb> schemaFactory;

  @Inject
  GetVerifications(SchemaFactory<CiDb> schemaFactory) {
    this.schemaFactory = schemaFactory;
  }

  @Override
  public Map<String, VerificationInfo> apply(RevisionResource rsrc)
      throws IOException, OrmException {
    Map<String, VerificationInfo> out = Maps.newHashMap();
    try (CiDb db = schemaFactory.open()) {
      for (PatchSetVerification v : db.patchSetVerifications()
          .byPatchSet(rsrc.getPatchSet().getId())) {
        VerificationInfo info = new VerificationInfo();
        info.value = v.getValue();
        info.url = v.getUrl();
        info.verifier = v.getVerifier();
        info.comment = v.getComment();
        out.put(v.getLabelId().get(), info);
      }
    }
    return out;
  }
}
