package org.codeheadsystems.featureflag.model;

import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import org.immutables.value.Value;

/**
 * The interface Feature manager configuration.
 */
@Value.Immutable
public interface FeatureManagerConfiguration {

  /**
   * Cache maximum size int. Defaults to 100.
   *
   * @return the int
   */
  default int cacheMaximumSize() {
    return 100;
  }

  /**
   * Cache refresh after write duration. Defaults to 60 seconds.
   *
   * @return the duration
   */
  default Duration cacheRefreshAfterWrite() {
    return Duration.ofSeconds(60);
  }

  /**
   * Cache expire after access duration. Defaults to 600 seconds.
   *
   * @return the duration
   */
  default Duration cacheExpireAfterAccess() {
    return Duration.ofSeconds(600);
  }

  /**
   * Cache loader executor. Defaults to the common fork join pool.
   *
   * @return the executor
   */
  default Executor cacheLoaderExecutor() {
    return ForkJoinPool.commonPool();
  }

}
