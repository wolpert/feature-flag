package org.codeheadsystems.featureflag.manager;

import com.codeheadsystems.metrics.Metrics;
import com.codeheadsystems.metrics.Tags;
import java.util.Optional;
import org.codeheadsystems.featureflag.manager.FeatureManager.Decorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Metrics decorator.
 */
public class MetricsDecorator {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricsDecorator.class);

  private final Metrics metrics;

  /**
   * Instantiates a new Metrics decorator.
   *
   * @param metrics the metrics
   */
  public MetricsDecorator(final Metrics metrics) {
    this.metrics = metrics;
    LOGGER.info("MetricsDecorator({})", metrics);
  }

  /**
   * Feature manager decorator decorator.
   *
   * @return the decorator
   */
  public Decorator<FeatureManager> featureManagerDecorator() {
    return (delegate) -> {
      LOGGER.info("decorateFeatureManager({})", delegate);
      return new FeatureManager() {
        @Override
        public boolean isEnabled(String featureId, String discriminator) {
          final boolean isEnabled = delegate.isEnabled(featureId, discriminator);
          final Tags tags = Tags.of(
              "feature", featureId,
              "discriminator", discriminator,
              "enabled", Boolean.toString(isEnabled));
          metrics.increment("feature_flag_isEnabled", tags);
          return isEnabled;
        }

        @Override
        public void invalidate(final String featureId) {
          metrics.time("feature_flag_invalidate",
              Tags.of("feature", featureId),
              () -> {
                delegate.invalidate(featureId);
                return null;
              });
        }
      };
    };
  }

  /**
   * Feature lookup manager decorator decorator.
   *
   * @return the decorator
   */
  public Decorator<FeatureLookupManager> featureLookupManagerDecorator() {
    return (delegate) -> {
      LOGGER.info("decorateFeatureLookupManager({})", delegate);
      return new FeatureLookupManager() {
        @Override
        public Optional<Double> lookupPercentage(String featureId) {
          return metrics.time("feature_flag_lookup",
              Tags.of("feature", featureId),
              () -> delegate.lookupPercentage(featureId));
        }

        @Override
        public boolean setPercentage(String featureId, double percentage) {
          return metrics.time("feature_flag_setPercentage",
              Tags.of("feature", featureId),
              () -> delegate.setPercentage(featureId, percentage));
        }

        @Override
        public void deletePercentage(String featureId) {
          metrics.time("feature_flag_deletePercentage",
              Tags.of("feature", featureId),
              () -> {
                delegate.deletePercentage(featureId);
                return null;
              });
        }
      };
    };
  }

}
