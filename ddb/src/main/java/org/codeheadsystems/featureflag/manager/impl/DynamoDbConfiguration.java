package org.codeheadsystems.featureflag.manager.impl;

import org.immutables.value.Value;

/**
 * The interface Dynamo db configuration.
 */
@Value.Immutable
public interface DynamoDbConfiguration {

  /**
   * Table name string.
   *
   * @return the string
   */
  @Value.Default
  default String tableName() {
    return "feature_flag";
  }

  /**
   * Feature hash column string.
   *
   * @return the string
   */
  @Value.Default
  default String featureHashColumn() {
    return "feature_id";
  }

  /**
   * Percentage column string.
   *
   * @return the string
   */
  @Value.Default
  default String percentageColumn() {
    return "percentage";
  }

}
