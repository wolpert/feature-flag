package org.codeheadsystems.featureflag.manager.impl;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.util.concurrent.ExecutionException;
import org.codeheadsystems.featureflag.factory.Enablement;
import org.codeheadsystems.featureflag.factory.EnablementFactory;
import org.codeheadsystems.featureflag.manager.FeatureLookupManager;
import org.codeheadsystems.featureflag.manager.FeatureManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Enablement manager.
 */
public class FeatureManagerImpl implements FeatureManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(FeatureManagerImpl.class);

  private final EnablementFactory enablementFactory;
  private final FeatureLookupManager featureLookupManager;
  private final LoadingCache<String, Enablement> featureEnablementCache;

  /**
   * Instantiates a new Feature manager.
   *
   * @param builder The builder user to create this.
   */
  public FeatureManagerImpl(final Builder builder) {
    this.enablementFactory = builder.getEnablementFactory();
    this.featureLookupManager = builder.getFeatureLookupManager();
    this.featureEnablementCache = builder.getCacheBuilder()
        .build(CacheLoader.asyncReloading(
            CacheLoader.from(this::lookup),
            builder.getConfiguration().cacheLoaderExecutor()));
    LOGGER.info("FeatureManager({},{},{})", builder.getConfiguration(), featureLookupManager, enablementFactory);
  }

  private Enablement lookup(String featureId) {
    LOGGER.info("lookup({})", featureId);
    return featureLookupManager.lookupPercentage(featureId)
        .map(enablementFactory::generate)
        .orElseGet(enablementFactory::disabledFeature);
  }

  /**
   * Is enabled boolean.
   *
   * @param featureId     the feature id
   * @param discriminator the discriminator
   * @return the boolean
   */
  @Override
  public boolean isEnabled(String featureId, String discriminator) {
    try {
      return featureEnablementCache.get(featureId).enabled(discriminator);
    } catch (ExecutionException | UncheckedExecutionException e) {
      LOGGER.error("Error getting feature enablement for: {}:{}", featureId, discriminator, e);
      return false;
    }
  }

  /**
   * Invalidate the feature id in the cache.
   *
   * @param featureId the feature id
   */
  @Override
  public void invalidate(String featureId) {
    featureEnablementCache.invalidate(featureId);
  }


}
