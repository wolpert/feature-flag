package org.codeheadsystems.featureflag.manager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.time.Duration;
import java.util.function.Supplier;
import org.codeheadsystems.featureflag.factory.Enablement;
import org.codeheadsystems.featureflag.factory.EnablementFactory;
import org.codeheadsystems.featureflag.model.FeatureManagerConfiguration;
import org.codeheadsystems.featureflag.model.ImmutableFeatureManagerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Enablement manager.
 */
public class FeatureManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(FeatureManager.class);

  private final EnablementFactory enablementFactory;
  private final FeatureLookupManager featureLookupManager;
  private final LoadingCache<String, Enablement> featureEnablementCache;

  /**
   * Instantiates a new Feature manager.
   *
   * @param enablementFactory    the feature factory
   * @param featureLookupManager the feature lookup manager
   */
  private FeatureManager(final EnablementFactory enablementFactory,
                         final FeatureLookupManager featureLookupManager,
                         final FeatureManagerConfiguration configuration,
                         final CacheBuilder<String, Enablement> cacheBuilder) {
    LOGGER.info("FeatureManager({},{},{})", configuration, featureLookupManager, enablementFactory);
    this.enablementFactory = enablementFactory;
    this.featureLookupManager = featureLookupManager;
    this.featureEnablementCache = cacheBuilder
        .build(CacheLoader.asyncReloading(
            CacheLoader.from(this::lookup),
            configuration.cacheLoaderExecutor()));
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
  public boolean isEnabled(String featureId, String discriminator) {
    return featureEnablementCache.getUnchecked(featureId).enabled(discriminator);
  }

  /**
   * If enabled else t.
   *
   * @param <T>           the type parameter
   * @param featureId     the feature id
   * @param discriminator the discriminator
   * @param ifEnabled     the if enabled
   * @param ifDisabled    the if disabled
   * @return the t
   */
  public <T> T ifEnabledElse(String featureId, String discriminator, Supplier<T> ifEnabled, Supplier<T> ifDisabled) {
    return isEnabled(featureId, discriminator) ? ifEnabled.get() : ifDisabled.get();
  }

  /**
   * Invalidate the feature id in the cache.
   *
   * @param featureId the feature id
   */
  public void invalidate(String featureId) {
    featureEnablementCache.invalidate(featureId);
  }

  /**
   * The type Builder.
   */
  public static class Builder {
    private EnablementFactory enablementFactory;
    private FeatureLookupManager featureLookupManager;
    private FeatureManagerConfiguration configuration;
    private CacheBuilder<String, Enablement> cacheBuilder;

    private static CacheBuilder<String, Enablement> getDefaultCacheBuilder() {
      return CacheBuilder.newBuilder()
          .maximumSize(100) // oh god, like we will have 100 features?
          .refreshAfterWrite(Duration.ofSeconds(60)) // refresh from source every 60seconds
          .expireAfterAccess(Duration.ofSeconds(600)) // expire after 600 seconds of inactivity
          .removalListener(notification -> LOGGER.trace("removalListener({})", notification.getKey()));
    }

    /**
     * With enablement factory builder. Required to be called.
     *
     * @param enablementFactory the enablement factory
     * @return the builder
     */
    public Builder withEnablementFactory(final EnablementFactory enablementFactory) {
      this.enablementFactory = enablementFactory;
      return this;
    }

    /**
     * With feature lookup manager builder. Required to be called.
     *
     * @param featureLookupManager the feature lookup manager
     * @return the builder
     */
    public Builder withFeatureLookupManager(final FeatureLookupManager featureLookupManager) {
      this.featureLookupManager = featureLookupManager;
      return this;
    }

    /**
     * With configuration builder. Optional to be called.
     *
     * @param configuration the configuration
     * @return the builder
     */
    public Builder withConfiguration(final FeatureManagerConfiguration configuration) {
      this.configuration = configuration;
      return this;
    }

    /**
     * With configuration builder. Optional to be called.
     *
     * @param cacheBuilder the configuration
     * @return the builder
     */
    public Builder withCacheBuilder(final CacheBuilder<String, Enablement> cacheBuilder) {
      this.cacheBuilder = cacheBuilder;
      return this;
    }

    /**
     * Build feature manager.
     *
     * @return the feature manager
     */
    public FeatureManager build() {
      if (enablementFactory == null) {
        throw new IllegalStateException("Missing required fields: enablementFactory");
      }
      if (featureLookupManager == null) {
        throw new IllegalStateException("Missing required fields: featureLookupManager");
      }
      final FeatureManagerConfiguration buildConfig = this.configuration == null
          ? ImmutableFeatureManagerConfiguration.builder().build()
          : this.configuration;
      final CacheBuilder<String, Enablement> buildCacheBuilder = this.cacheBuilder == null
          ? getDefaultCacheBuilder()
          : this.cacheBuilder;
      return new FeatureManager(enablementFactory, featureLookupManager, buildConfig, buildCacheBuilder);
    }
  }

}
