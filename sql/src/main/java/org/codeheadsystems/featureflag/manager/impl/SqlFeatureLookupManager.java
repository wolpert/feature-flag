package org.codeheadsystems.featureflag.manager.impl;

import java.util.Objects;
import java.util.Optional;
import org.codeheadsystems.featureflag.manager.FeatureLookupManager;
import org.jdbi.v3.cache.caffeine.CaffeineCachePlugin;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * The type Sql feature lookup manager, as a dao.
 */
public interface SqlFeatureLookupManager extends FeatureLookupManager {

  @Override
  @SqlQuery("select PERCENTAGE from FEATURE_FLAG where FEATURE_ID = :featureId")
  Optional<Double> lookupPercentage(@Bind("featureId") final String featureId);

  /**
   * Internal insert of the feature flag with the percentage.
   *
   * @param featureId  the feature id
   * @param percentage the percentage
   * @return the int
   */
  @SqlUpdate("insert into FEATURE_FLAG (FEATURE_ID, PERCENTAGE) values (:featureId, :percentage)")
  int insert(@Bind("featureId") final String featureId, @Bind("percentage") final double percentage);

  /**
   * Internal update to set the percentage for the feature flag.
   *
   * @param featureId  the feature id
   * @param percentage the percentage
   * @return the int
   */
  @SqlUpdate("update FEATURE_FLAG set PERCENTAGE = :percentage where FEATURE_ID = :featureId")
  int update(@Bind("featureId") final String featureId, @Bind("percentage") final double percentage);

  @Override
  default boolean setPercentage(final String featureId, final double percentage) {
    if (update(featureId, percentage) == 0) {
      return insert(featureId, percentage) == 1;
    }
    return true;
  }

  @Override
  @SqlUpdate("delete from FEATURE_FLAG where FEATURE_ID = :featureId")
  void deletePercentage(@Bind("featureId") final String featureId);

  /**
   * The type Builder.
   */
  class Builder {
    private Jdbi jdbi;

    /**
     * Jdbi builder.
     *
     * @param jdbi the jdbi
     * @return the builder
     */
    public Builder jdbi(final Jdbi jdbi) {
      this.jdbi = jdbi;
      return this;
    }

    /**
     * Build sql feature lookup manager.
     *
     * @return the sql feature lookup manager
     */
    public SqlFeatureLookupManager build() {
      Objects.requireNonNull(jdbi, "jdbi must be set");
      jdbi.installPlugin(new SqlObjectPlugin())
          .installPlugin(new CaffeineCachePlugin());
      return jdbi.onDemand(SqlFeatureLookupManager.class);
    }

  }

}
