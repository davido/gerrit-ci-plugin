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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.base.Strings;
import com.google.gerrit.common.Nullable;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.extensions.persistence.DataSourceInterceptor;
import com.google.gerrit.metrics.CallbackMetric1;
import com.google.gerrit.metrics.Description;
import com.google.gerrit.metrics.Field;
import com.google.gerrit.metrics.MetricMaker;
import com.google.gerrit.server.config.ConfigUtil;
import com.google.gerrit.server.config.PluginConfig;
import com.google.gerrit.server.config.SitePaths;
import com.google.gwtorm.jdbc.SimpleDataSource;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;

import org.apache.commons.dbcp.BasicDataSource;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

/** Provides access to the DataSource. */
@Singleton
public class CiDataSourceProvider implements Provider<DataSource>,
    LifecycleListener {
  public static final int DEFAULT_POOL_LIMIT = 8;

  private final MetricMaker metrics;
  private final Context ctx;
  private final CiDataSourceType dst;
  private final PluginConfig config;
  private DataSource ds;

  @Inject
  protected CiDataSourceProvider(SitePaths site,
      @PluginName String pluginName,
      @Nullable MetricMaker metrics,
      Context ctx,
      CiDataSourceType dst) {
    File file = site.gerrit_config.toFile();
    FileBasedConfig cfg = new FileBasedConfig(file, FS.DETECTED);
    try {
      cfg.load();
    } catch (IOException | ConfigInvalidException e) {
      throw new ProvisionException(e.getMessage(), e);
    }
    this.config = new PluginConfig(pluginName, cfg);
    this.metrics = metrics;
    this.ctx = ctx;
    this.dst = dst;
  }

  @Override
  public synchronized DataSource get() {
    if (ds == null) {
      ds = open(ctx, dst);
    }
    return ds;
  }

  @Override
  public void start() {
  }

  @Override
  public synchronized void stop() {
    if (ds instanceof BasicDataSource) {
      try {
        ((BasicDataSource) ds).close();
      } catch (SQLException e) {
        // Ignore the close failure.
      }
    }
  }

  public static enum Context {
    SINGLE_USER, MULTI_USER
  }

  private DataSource open(Context context, CiDataSourceType dst) {
    //ConfigSection dbs = new ConfigSection(cfg, "database");
    String driver = config.getString("driver");
    if (Strings.isNullOrEmpty(driver)) {
      driver = dst.getDriver();
    }

    String url = config.getString("dbUrl");
    if (Strings.isNullOrEmpty(url)) {
      url = dst.getUrl();
    }

    String username = config.getString("username");
    String password = config.getString("password");
    String interceptor = config.getString("dataSourceInterceptorClass");

    boolean usePool;
    if (context == Context.SINGLE_USER) {
      usePool = false;
    } else {
      usePool = config.getBoolean("connectionpool", dst.usePool());
    }

    if (usePool) {
      final BasicDataSource ds = new BasicDataSource();
      ds.setDriverClassName(driver);
      ds.setUrl(url);
      if (username != null && !username.isEmpty()) {
        ds.setUsername(username);
      }
      if (password != null && !password.isEmpty()) {
        ds.setPassword(password);
      }
      ds.setMaxActive(config.getInt("poollimit", DEFAULT_POOL_LIMIT));
      ds.setMinIdle(config.getInt("poolminidle", 4));
      ds.setMaxIdle(config.getInt("poolmaxidle", 4));
      String valueString = config.getString("poolmaxwait");
      if (Strings.isNullOrEmpty(valueString)) {
        ds.setMaxWait(MILLISECONDS.convert(30, SECONDS));
      } else {
        ds.setMaxWait(ConfigUtil.getTimeUnit(
            valueString, MILLISECONDS.convert(30, SECONDS), MILLISECONDS));
      }
      ds.setInitialSize(ds.getMinIdle());
      exportPoolMetrics(ds);
      return intercept(interceptor, ds);
    } else {
      // Don't use the connection pool.
      //
      try {
        Properties p = new Properties();
        p.setProperty("driver", driver);
        p.setProperty("url", url);
        if (username != null) {
          p.setProperty("user", username);
        }
        if (password != null) {
          p.setProperty("password", password);
        }
        return intercept(interceptor, new SimpleDataSource(p));
      } catch (SQLException se) {
        throw new ProvisionException("Database unavailable", se);
      }
    }
  }

  private void exportPoolMetrics(final BasicDataSource pool) {
    final CallbackMetric1<Boolean, Integer> cnt = metrics.newCallbackMetric(
        "sql/connection_pool/connections",
        Integer.class,
        new Description("SQL database connections")
          .setGauge()
          .setUnit("connections"),
        Field.ofBoolean("active"));
    metrics.newTrigger(cnt, new Runnable() {
      @Override
      public void run() {
        synchronized (pool) {
          cnt.set(true, pool.getNumActive());
          cnt.set(false, pool.getNumIdle());
        }
      }
    });
  }

  private DataSource intercept(String interceptor, DataSource ds) {
    if (interceptor == null) {
      return ds;
    }
    try {
      Constructor<?> c = Class.forName(interceptor).getConstructor();
      DataSourceInterceptor datasourceInterceptor =
          (DataSourceInterceptor) c.newInstance();
      return datasourceInterceptor.intercept("CiDb", ds);
    } catch (ClassNotFoundException | SecurityException | NoSuchMethodException
        | IllegalArgumentException | InstantiationException
        | IllegalAccessException | InvocationTargetException e) {
      throw new ProvisionException("Cannot intercept datasource", e);
    }
  }
}
