package org.codeheadsystems.featureflag.factory.impl;

import java.util.Optional;
import org.codeheadsystems.featureflag.manager.FeatureLookupManager;
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

  @SqlUpdate("insert into FEATURE_FLAG (FEATURE_ID, PERCENTAGE) values (:featureId, :percentage)")
  int insert(@Bind("featureId") final String featureId, @Bind("percentage") final double percentage);

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
}
