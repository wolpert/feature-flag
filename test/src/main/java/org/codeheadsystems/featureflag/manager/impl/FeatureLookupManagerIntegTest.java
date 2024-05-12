package org.codeheadsystems.featureflag.manager.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;
import org.codeheadsystems.featureflag.manager.FeatureLookupManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Extend this to get your validation suite.
 */
public abstract class FeatureLookupManagerIntegTest {

  private String featureId;

  /**
   * Manager feature lookup manager.
   *
   * @return the feature lookup manager
   */
  public abstract FeatureLookupManager manager();


  /**
   * Sets feature id.
   */
  @BeforeEach
  void setupFeatureId() {
    featureId = UUID.randomUUID().toString();
  }


  /**
   * Lookup percentage found.
   */
  @Test
  void lookupPercentage_found() {
    final FeatureLookupManager manager = manager();
    ;
    manager.setPercentage(featureId, 0.5);
    Optional<Double> result = manager.lookupPercentage(featureId);
    assertThat(result).isNotNull()
        .isNotEmpty()
        .contains(0.5);
  }

  /**
   * Lookup percentage not found.
   */
  @Test
  void lookupPercentage_notFound() {
    final FeatureLookupManager manager = manager();
    ;
    Optional<Double> result = manager.lookupPercentage(featureId);
    assertThat(result).isNotNull()
        .isEmpty();
  }


  /**
   * Delete percentage.
   *
   * @throws InterruptedException the interrupted exception
   */
  @Test
  void deletePercentage() throws InterruptedException {
    final FeatureLookupManager manager = manager();
    ;
    manager.setPercentage(featureId, 0.5);
    assertThat(manager.lookupPercentage(featureId)).isNotEmpty().contains(0.5);
    manager.deletePercentage(featureId);
    assertThat(manager.lookupPercentage(featureId)).isEmpty();
  }

}
