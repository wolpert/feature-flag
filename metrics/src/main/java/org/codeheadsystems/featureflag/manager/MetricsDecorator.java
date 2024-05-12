package org.codeheadsystems.featureflag.manager;

import com.codeheadsystems.metrics.Metrics;
import io.micrometer.core.instrument.Tags;
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
    return (featureManager) -> {
      LOGGER.info("decorateFeatureManager({})", featureManager);
      return new FeatureManager() {
        @Override
        public boolean isEnabled(String featureId, String discriminator) {
          final boolean isEnabled = featureManager.isEnabled(featureId, discriminator);
          final Tags tags = Tags.of(
              "feature", featureId,
              "discriminator", discriminator,
              "enabled", Boolean.toString(isEnabled));
          metrics.counter("feature_flag_isEnabled", tags);
          return isEnabled;
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
    return (featureLookupManager) -> {
      LOGGER.info("decorateFeatureLookupManager({})", featureLookupManager);
      return new FeatureLookupManager() {
        @Override
        public Optional<Double> lookupPercentage(String featureId) {
          return metrics.time("feature_flag_lookup",
              Tags.of("feature", featureId),
              () -> featureLookupManager.lookupPercentage(featureId));
        }

        @Override
        public boolean setPercentage(String featureId, double percentage) {
          return metrics.time("feature_flag_setPercentage",
              Tags.of("feature", featureId),
              () -> featureLookupManager.setPercentage(featureId, percentage));
        }

        @Override
        public void deletePercentage(String featureId) {
          metrics.time("feature_flag_deletePercentage",
              Tags.of("feature", featureId),
              () -> {
                featureLookupManager.deletePercentage(featureId);
                return null;
              });
        }
      };
    };
  }

}
