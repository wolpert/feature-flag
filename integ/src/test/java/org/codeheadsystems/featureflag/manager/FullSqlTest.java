package org.codeheadsystems.featureflag.manager;

import com.codeheadsystems.metrics.Metrics;
import org.codeheadsystems.featureflag.manager.impl.DdbFeatureLookupManager;
import org.codeheadsystems.featureflag.manager.impl.DynamoDbConfiguration;
import org.codeheadsystems.featureflag.manager.impl.ImmutableDynamoDbConfiguration;
import org.codeheadsystems.featureflag.manager.impl.SqlFeatureLookupManager;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * The type Full sql test.
 */
public class FullSqlTest {

  /**
   * The Feature manager.
   */
  FeatureManager featureManager;

  /**
   * Sets dynamo db.
   *
   * @param metrics  the metrics
   * @param dbClient the db client
   */
  void setupDynamoDb(Metrics metrics, DynamoDbClient dbClient) {
    MetricsDecorator metricsDecorator = new MetricsDecorator(metrics);
    DynamoDbConfiguration dbConfiguration = ImmutableDynamoDbConfiguration.builder().build();
    featureManager = new FeatureManager.Builder()
        .withFeatureManagerDecorator(metricsDecorator.featureManagerDecorator())
        .withFeatureLookupManagerDecorator(metricsDecorator.featureLookupManagerDecorator())
        .withFeatureLookupManager(new DdbFeatureLookupManager(dbConfiguration, dbClient))
        .build();
  }

  /**
   * Calculate result string.
   *
   * @param customerId the customer id
   * @return the string
   */
  String calculateResult(String customerId) {
    if (featureManager.isEnabled("updatedCalculation", customerId)) {
      return existingProcess(customerId);
    } else {
      return newProcess(customerId);
    }
  }

  /**
   * Calculate result callables string.
   *
   * @param customerId the customer id
   * @return the string
   */
  String calculateResultCallables(String customerId) {
    return featureManager.ifEnabledElse("updatedCalculation", customerId,
        () -> existingProcess(customerId),
        () -> newProcess(customerId));
  }

  /**
   * Sets sql.
   *
   * @param metrics the metrics
   * @param jdbi    the jdbi
   */
  void setupSql(Metrics metrics, Jdbi jdbi) {
    MetricsDecorator metricsDecorator = new MetricsDecorator(metrics);
    featureManager = new FeatureManager.Builder()
        .withFeatureManagerDecorator(metricsDecorator.featureManagerDecorator())
        .withFeatureLookupManagerDecorator(metricsDecorator.featureLookupManagerDecorator())
        .withFeatureLookupManager(new SqlFeatureLookupManager.Builder().jdbi(jdbi).build())
        .build();
  }

  @Test
  void temporaryTest() {
    // This is a temporary test
  }

  /**
   * Existing process string.
   *
   * @param customerId the customer id
   * @return the string
   */
  public String existingProcess(String customerId) {
    return "This is the crrent way";
  }

  /**
   * New process string.
   *
   * @param customerId the customer id
   * @return the string
   */
  public String newProcess(String customerId) {
    return "This is the other way";
  }

}
