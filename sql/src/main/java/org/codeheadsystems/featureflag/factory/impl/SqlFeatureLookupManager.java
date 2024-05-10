package org.codeheadsystems.featureflag.factory.impl;

import java.util.Optional;
import org.codeheadsystems.featureflag.manager.FeatureLookupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Sql feature lookup manager.
 */
public class SqlFeatureLookupManager implements FeatureLookupManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(SqlFeatureLookupManager.class);

  @Override
  public Optional<Double> lookupPercentage(final String featureId) {
    return Optional.empty();
  }

  @Override
  public boolean setPercentage(final String featureId, final double percentage) {
    return false;
  }

  @Override
  public void deletePercentage(final String featureId) {

  }
}
