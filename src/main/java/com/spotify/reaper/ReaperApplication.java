/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.spotify.reaper;

import com.spotify.reaper.resources.ClusterResource;
import com.spotify.reaper.resources.PingResource;
import com.spotify.reaper.resources.ReaperHealthCheck;
import com.spotify.reaper.resources.RepairRunResource;
import com.spotify.reaper.resources.TableResource;
import com.spotify.reaper.service.RepairRunner;
import com.spotify.reaper.storage.IStorage;
import com.spotify.reaper.storage.MemoryStorage;
import com.spotify.reaper.storage.PostgresStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class ReaperApplication extends Application<ReaperApplicationConfiguration> {

  private static final Logger LOG = LoggerFactory.getLogger(ReaperApplication.class);

  public static void main(String[] args) throws Exception {
    new ReaperApplication().run(args);
  }

  @Override
  public String getName() {
    return "cassandra-reaper";
  }

  @Override
  public void initialize(Bootstrap<ReaperApplicationConfiguration> bootstrap) {
    LOG.debug("ReaperApplication.initialize called");
  }

  @Override
  public void run(ReaperApplicationConfiguration config,
                  Environment environment) throws ReaperException {

    LOG.info("initializing runner thread pool with {} threads", config.getRepairRunThreadCount());
    RepairRunner.initializeThreadPool(config.getRepairRunThreadCount());

    LOG.info("initializing storage of type: {}", config.getStorageType());
    IStorage storage = initializeStorage(config, environment);

    LOG.info("creating and registering health checks");
    // Notice that health checks are registered under the admin application on /healthcheck
    final ReaperHealthCheck healthCheck = new ReaperHealthCheck(storage);
    environment.healthChecks().register("reaper", healthCheck);
    environment.jersey().register(healthCheck);

    LOG.info("creating resources and registering endpoints");
    final PingResource pingResource = new PingResource();
    environment.jersey().register(pingResource);

    final ClusterResource addClusterResource = new ClusterResource(storage);
    environment.jersey().register(addClusterResource);

    final TableResource addTableResource = new TableResource(config, storage);
    environment.jersey().register(addTableResource);

    final RepairRunResource addRepairRunResource = new RepairRunResource(storage);
    environment.jersey().register(addRepairRunResource);

    LOG.info("Reaper is ready to accept connections");
  }

  private IStorage initializeStorage(ReaperApplicationConfiguration config,
                                     Environment environment) throws ReaperException {
    IStorage storage;
    if (config.getStorageType().equalsIgnoreCase("memory")) {
      storage = new MemoryStorage();
    } else if (config.getStorageType().equalsIgnoreCase("database")) {
      storage = new PostgresStorage(config, environment);
    } else {
      LOG.error("invalid storageType: {}", config.getStorageType());
      throw new ReaperException("invalid storage type: " + config.getStorageType());
    }
    assert storage.isStorageConnected() : "Failed to connect storage";
    return storage;
  }

}
