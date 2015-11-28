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

package com.googlesource.gerrit.plugins.ci.init;

import static com.google.inject.Scopes.SINGLETON;
import static com.google.inject.Stage.PRODUCTION;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.lifecycle.LifecycleModule;
import com.google.gerrit.metrics.DisabledMetricMaker;
import com.google.gerrit.metrics.MetricMaker;
import com.google.gerrit.pgm.init.api.ConsoleUI;
import com.google.gerrit.pgm.init.api.InitStep;
import com.google.gerrit.pgm.init.api.Section;
import com.google.gerrit.reviewdb.client.CurrentSchemaVersion;
import com.google.gerrit.server.config.SitePaths;
import com.google.gwtorm.jdbc.JdbcExecutor;
import com.google.gwtorm.jdbc.JdbcSchema;
import com.google.gwtorm.server.OrmException;
import com.google.gwtorm.server.SchemaFactory;
import com.google.gwtorm.server.StatementExecutor;
import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import com.googlesource.gerrit.plugins.ci.server.CiDb;
import com.googlesource.gerrit.plugins.ci.server.schema.CiDataSourceModule;
import com.googlesource.gerrit.plugins.ci.server.schema.CiDataSourceProvider;
import com.googlesource.gerrit.plugins.ci.server.schema.CiDataSourceType;
import com.googlesource.gerrit.plugins.ci.server.schema.CiDataSourceTypeGuesser;
import com.googlesource.gerrit.plugins.ci.server.schema.CiDatabaseModule;
import com.googlesource.gerrit.plugins.ci.server.schema.SchemaVersion;
import com.googlesource.gerrit.plugins.ci.server.schema.UpdateUI;

import java.lang.annotation.Annotation;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

@Singleton
public class InitPlugin implements InitStep {
  private final ConsoleUI ui;
  private final SitePaths site;
  private final Section configSection;
  private final Injector parent;
  private SchemaFactory<CiDb> dbFactory;
  private Provider<SchemaVersion> updater;

  @Inject
  InitPlugin(Section.Factory sections,
      @PluginName String pluginName,
      ConsoleUI ui,
      SitePaths site,
      Injector parent) {
    this.ui = ui;
    this.site = site;
    this.configSection = sections.get("plugin", pluginName);
    this.parent = parent;
  }

  @Override
  public void run() throws Exception {
    ui.header("SQL Database for CI plugin");

    Set<String> allowedValues = Sets.newTreeSet();
    Injector i = Guice.createInjector(PRODUCTION,
        new DatabaseConfigModule(site));

    List<Binding<DatabaseConfigInitializer>> dbConfigBindings =
        i.findBindingsByType(new TypeLiteral<DatabaseConfigInitializer>() {});
    for (Binding<DatabaseConfigInitializer> binding : dbConfigBindings) {
      Annotation annotation = binding.getKey().getAnnotation();
      if (annotation instanceof Named) {
        allowedValues.add(((Named) annotation).value());
      }
    }

    if (!Strings.isNullOrEmpty(configSection.get("dbUrl"))
        && Strings.isNullOrEmpty(configSection.get("dbType"))) {
      configSection.set("dbType", "h2");
    }

    String dbType =
        configSection.select("Database server type", "dbType", "h2",
            allowedValues);

    DatabaseConfigInitializer dci =
        i.getInstance(Key.get(DatabaseConfigInitializer.class,
            Names.named(dbType.toLowerCase())));

    /** TODO(davido): We probably don't need that, as
     * CI database would be from the same type as
     * ReviewDb. So we expect that the needed libraries
     * were already installed.
     *
    if (dci instanceof MySqlInitializer) {
      libraries.mysqlDriver.downloadRequired();
    } else if (dci instanceof OracleInitializer) {
      libraries.oracleDriver.downloadRequired();
    } else if (dci instanceof DB2Initializer) {
      libraries.db2Driver.downloadRequired();
    }
    **/

    dci.initConfig(configSection);
  }

  @Override
  public void postRun() throws Exception {
    Injector i = buildInjector(parent);
    updater = i.getProvider(SchemaVersion.class);
    this.dbFactory = i.getInstance(
        Key.get(
            new TypeLiteral<SchemaFactory<CiDb>>() {}));
    upgradeSchema();
  }

  private Injector buildInjector(final Injector parent) {
    List<Module> modules = new ArrayList<>();

    modules.add(new LifecycleModule() {
      @Override
      protected void configure() {
        // For bootstrap we need to retrieve the ds type first
        CiDataSourceTypeGuesser guesser =
            parent.createChildInjector(
                new CiDataSourceModule()).getInstance(
                    Key.get(CiDataSourceTypeGuesser.class));

        // For the ds type we retrieve the underlying implementation
        CiDataSourceType dst = parent.createChildInjector(
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

    modules.add(new AbstractModule() {
      @Override
      protected void configure() {
        bind(SchemaVersion.class).to(SchemaVersion.C);
        bind(MetricMaker.class).to(DisabledMetricMaker.class);
      }
    });

    return parent.createChildInjector(modules);
  }

  private void upgradeSchema() throws OrmException {
    final List<String> pruneList = new ArrayList<>();
    update(new UpdateUI() {
      @Override
      public void message(String msg) {
        System.err.println(msg);
        System.err.flush();
      }

      @Override
      public boolean yesno(boolean def, String msg) {
        return ui.yesno(def, msg);
      }

      @Override
      public boolean isBatch() {
        return ui.isBatch();
      }

      @Override
      public void pruneSchema(StatementExecutor e, List<String> prune) {
        for (String p : prune) {
          if (!pruneList.contains(p)) {
            pruneList.add(p);
          }
        }
      }
    });

    if (!pruneList.isEmpty()) {
      StringBuilder msg = new StringBuilder();
      msg.append("Execute the following SQL to drop unused objects:\n");
      msg.append("\n");
      for (String sql : pruneList) {
        msg.append("  ");
        msg.append(sql);
        msg.append(";\n");
      }

      if (ui.isBatch()) {
        System.err.print(msg);
        System.err.flush();

      } else if (ui.yesno(true, "%s\nExecute now", msg)) {
        try (JdbcSchema db = (JdbcSchema) dbFactory.open();
            JdbcExecutor e = new JdbcExecutor(db)) {
          for (String sql : pruneList) {
            e.execute(sql);
          }
        }
      }
    }
  }

  public void update(UpdateUI ui) throws OrmException {
    try (CiDb db = dbFactory.open()) {
      SchemaVersion u = updater.get();
      CurrentSchemaVersion version = getSchemaVersion(db);
      if (version == null) {
          try (JdbcExecutor e = new JdbcExecutor((JdbcSchema) db)) {
            ((JdbcSchema) db).updateSchema(e);
          }
          final CurrentSchemaVersion sVer = CurrentSchemaVersion.create();
          sVer.versionNbr = SchemaVersion.getBinaryVersion();
          db.schemaVersion().insert(Collections.singleton(sVer));
      } else {
        try {
          u.check(ui, version, db);
        } catch (SQLException e) {
          throw new OrmException("Cannot upgrade schema", e);
        }
      }
    }
  }

  private CurrentSchemaVersion getSchemaVersion(CiDb db) {
    try {
      return db.schemaVersion().get(new CurrentSchemaVersion.Key());
    } catch (OrmException e) {
      return null;
    }
  }
}
