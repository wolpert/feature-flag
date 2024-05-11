package org.codeheadsystems.featureflag.manager.impl;

import com.codeheadsystems.test.datastore.DataStore;
import com.codeheadsystems.test.datastore.DynamoDbExtension;
import org.codeheadsystems.featureflag.manager.FeatureLookupManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@ExtendWith(DynamoDbExtension.class)
class DdbFeatureLookupManagerIntegTest extends FeatureLookupManagerIntegTest {

  private static final DynamoDbConfiguration DB_CONFIGURATION = ImmutableDynamoDbConfiguration.builder().build();

  @DataStore private DynamoDbClient dbClient;

  @BeforeEach
  void setupDatabase() {
    final DdbControlPlane ddbControlPlane = new DdbControlPlane(DB_CONFIGURATION, dbClient);
    if (!ddbControlPlane.isTableSetupCorrectly()) {
      ddbControlPlane.setupTable();
    }
  }

  @Override
  public FeatureLookupManager manager() {
    return new DdbFeatureLookupManager(DB_CONFIGURATION, dbClient);
  }

}