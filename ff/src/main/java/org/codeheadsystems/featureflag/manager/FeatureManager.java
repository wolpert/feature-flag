package org.codeheadsystems.featureflag.manager;

import com.google.common.cache.CacheBuilder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.codeheadsystems.featureflag.factory.Enablement;
import org.codeheadsystems.featureflag.factory.EnablementFactory;
import org.codeheadsystems.featureflag.manager.impl.FeatureManagerImpl;
import org.codeheadsystems.featureflag.model.FeatureManagerConfiguration;
import org.codeheadsystems.featureflag.model.ImmutableFeatureManagerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The interface Feature manager.
 */
public interface FeatureManager {

  /**
   * Is enabled boolean.
   *
   * @param featureId     the feature id
   * @param discriminator the discriminator
   * @return the boolean
   */
  boolean isEnabled(String featureId, String discriminator);


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
  default <T> T ifEnabledElse(String featureId, String discriminator, Supplier<T> ifEnabled, Supplier<T> ifDisabled) {
    return isEnabled(featureId, discriminator) ? ifEnabled.get() : ifDisabled.get();
  }

  /**
   * Invalidate the feature id in the cache.
   *
   * @param featureId the feature id
   */
  void invalidate(String featureId);

  /**
   * The interface Decorator.
   *
   * @param <T> the type parameter
   */
  interface Decorator<T> {

    /**
     * Decorate t.
     *
     * @param target the target
     * @return the t
     */
    T decorate(T target);

  }

  /**
   * The type Builder.
   */
  class Builder {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureManager.class);

    private EnablementFactory enablementFactory;
    private FeatureLookupManager featureLookupManager;
    private FeatureManagerConfiguration configuration;
    private CacheBuilder<String, Enablement> cacheBuilder;
    private List<Decorator<FeatureManager>> featureManagerDecorator = new ArrayList<>();
    private List<Decorator<FeatureLookupManager>> featureLookupManagerDecorator = new ArrayList<>();

    private static CacheBuilder<String, Enablement> getDefaultCacheBuilder() {
      return CacheBuilder.newBuilder()
          .maximumSize(100) // oh god, like we will have 100 features?
          .refreshAfterWrite(Duration.ofSeconds(60)) // refresh from source every 60seconds
          .expireAfterAccess(Duration.ofSeconds(600)) // expire after 600 seconds of inactivity
          .removalListener(notification -> LOGGER.trace("removalListener({})", notification.getKey()));
    }

    /**
     * With feature manager decorator builder.
     *
     * @param decorator the decorator
     * @return the builder
     */
    public Builder withFeatureManagerDecorator(Decorator<FeatureManager> decorator) {
      featureManagerDecorator.add(decorator);
      return this;
    }

    /**
     * Feature lookup manager decorator builder.
     *
     * @param decorator the decorator
     * @return the builder
     */
    public Builder withFeatureLookupManagerDecorator(Decorator<FeatureLookupManager> decorator) {
      featureLookupManagerDecorator.add(decorator);
      return this;
    }

    /**
     * With enablement factory builder. Required to be called.
     *
     * @param enablementFactory the enablement factory
     * @return the builder
     */
    public FeatureManagerImpl.Builder withEnablementFactory(final EnablementFactory enablementFactory) {
      this.enablementFactory = enablementFactory;
      return this;
    }

    /**
     * With feature lookup manager builder. Required to be called.
     *
     * @param featureLookupManager the feature lookup manager
     * @return the builder
     */
    public FeatureManagerImpl.Builder withFeatureLookupManager(final FeatureLookupManager featureLookupManager) {
      this.featureLookupManager = featureLookupManager;
      return this;
    }

    /**
     * With configuration builder. Optional to be called.
     *
     * @param configuration the configuration
     * @return the builder
     */
    public FeatureManagerImpl.Builder withConfiguration(final FeatureManagerConfiguration configuration) {
      this.configuration = configuration;
      return this;
    }

    /**
     * With configuration builder. Optional to be called.
     *
     * @param cacheBuilder the configuration
     * @return the builder
     */
    public FeatureManagerImpl.Builder withCacheBuilder(final CacheBuilder<String, Enablement> cacheBuilder) {
      this.cacheBuilder = cacheBuilder;
      return this;
    }

    /**
     * Build feature manager.
     *
     * @return the feature manager
     */
    public FeatureManager build() {
      FeatureLookupManager internalLookupManager = Objects.requireNonNull(featureLookupManager, "Missing required fields: featureLookupManager");
      enablementFactory = Objects.requireNonNullElse(enablementFactory, new EnablementFactory());
      configuration = Objects.requireNonNullElse(configuration, ImmutableFeatureManagerConfiguration.builder().build());
      cacheBuilder = Objects.requireNonNullElse(cacheBuilder, getDefaultCacheBuilder());

      for (Decorator<FeatureLookupManager> decorator : featureLookupManagerDecorator) {
        LOGGER.info("Decorating featureLookupManager with {}", decorator);
        internalLookupManager = decorator.decorate(internalLookupManager);
      }

      FeatureManager featureManager = new FeatureManagerImpl(this);
      for (Decorator<FeatureManager> decorator : featureManagerDecorator) {
        LOGGER.info("Decorating featureManager with {}", decorator);
        featureManager = decorator.decorate(featureManager);
      }
      return featureManager;
    }

    /**
     * Gets enablement factory.
     *
     * @return the enablement factory
     */
    public EnablementFactory getEnablementFactory() {
      return enablementFactory;
    }

    /**
     * Gets feature lookup manager.
     *
     * @return the feature lookup manager
     */
    public FeatureLookupManager getFeatureLookupManager() {
      return featureLookupManager;
    }

    /**
     * Gets configuration.
     *
     * @return the configuration
     */
    public FeatureManagerConfiguration getConfiguration() {
      return configuration;
    }

    /**
     * Gets cache builder.
     *
     * @return the cache builder
     */
    public CacheBuilder<String, Enablement> getCacheBuilder() {
      return cacheBuilder;
    }
  }

}
