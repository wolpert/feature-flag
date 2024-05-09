package org.codeheadsystems.featureflag.model;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import org.immutables.value.Value;

/**
 * The interface Feature manager configuration.
 */
@Value.Immutable
public interface FeatureManagerConfiguration {

  /**
   * Cache loader executor. Defaults to the common fork join pool.
   *
   * @return the executor
   */
  default Executor cacheLoaderExecutor() {
    return ForkJoinPool.commonPool();
  }

}
