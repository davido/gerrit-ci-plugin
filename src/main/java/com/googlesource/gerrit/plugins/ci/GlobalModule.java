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

import static com.google.inject.Scopes.SINGLETON;

import com.google.gerrit.extensions.config.FactoryModule;
import com.google.gerrit.lifecycle.LifecycleModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;

import com.googlesource.gerrit.plugins.ci.server.schema.CiDataSourceModule;
import com.googlesource.gerrit.plugins.ci.server.schema.CiDataSourceProvider;
import com.googlesource.gerrit.plugins.ci.server.schema.CiDataSourceType;
import com.googlesource.gerrit.plugins.ci.server.schema.CiDataSourceTypeGuesser;
import com.googlesource.gerrit.plugins.ci.server.schema.CiDatabaseModule;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

public class GlobalModule extends FactoryModule {

  private final Injector injector;

  @Inject
  GlobalModule(Injector injector) {
    this.injector = injector;
  }

  @Override
  protected void configure() {
    List<Module> modules = new ArrayList<>();
    modules.add(new LifecycleModule() {
      @Override
      protected void configure() {
        // For bootstrap we need to retrieve the ds type first
        CiDataSourceTypeGuesser guesser =
            injector.createChildInjector(
                new CiDataSourceModule()).getInstance(
                    Key.get(CiDataSourceTypeGuesser.class));

        // For the ds type we retrieve the underlying implementation
        CiDataSourceType dst = injector.createChildInjector(
            new CiDataSourceModule()).getInstance(
                Key.get(CiDataSourceType.class,
                    Names.named(guesser.guessDataSourceType())));
        // Bind the type to the retrieved instance
        bind(CiDataSourceType.class).toInstance(dst);
        bind(CiDataSourceProvider.Context.class).toInstance(
            CiDataSourceProvider.Context.MULTI_USER);
        bind(Key.get(DataSource.class, Names.named("CiDb"))).toProvider(
            CiDataSourceProvider.class).in(SINGLETON);
        listener().to(CiDataSourceProvider.class);
      }
    });
    modules.add(new CiDatabaseModule());
    for (Module module : modules) {
      install(module);
    }
  }
}
