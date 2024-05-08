package org.codeheadsystems.featureflag.manager.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.test.datastore.DataStore;
import com.codeheadsystems.test.datastore.DynamoDbExtension;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@ExtendWith(DynamoDbExtension.class)
class DdbFeatureLookupManagerIntegTest {

  private static final DynamoDbConfiguration DB_CONFIGURATION = ImmutableDynamoDbConfiguration.builder().build();

  @DataStore private DynamoDbClient dbClient;

  private DdbFeatureLookupManager ddbFeatureLookupManager;
  private String featureId;

  @BeforeEach
  void setup() {
    ddbFeatureLookupManager = new DdbFeatureLookupManager(DB_CONFIGURATION, dbClient);
    featureId = UUID.randomUUID().toString();
  }

  @BeforeEach
  void setupDatabase() {
    final DdbControlPlane ddbControlPlane = new DdbControlPlane(DB_CONFIGURATION, dbClient);
    if (!ddbControlPlane.isTableSetupCorrectly()) {
      ddbControlPlane.setupTable();
    }
  }


  @Test
  void lookupPercentage_found() {
    ddbFeatureLookupManager.setPercentage(featureId, 0.5);
    Optional<Double> result = ddbFeatureLookupManager.lookupPercentage(featureId);
    assertThat(result).isNotNull()
        .isNotEmpty()
        .contains(0.5);
  }

  @Test
  void lookupPercentage_notFound() {
    Optional<Double> result = ddbFeatureLookupManager.lookupPercentage(featureId);
    assertThat(result).isNotNull()
        .isEmpty();
  }


  @Test
  void deletePercentage() throws InterruptedException {
    ddbFeatureLookupManager.setPercentage(featureId, 0.5);
    assertThat(ddbFeatureLookupManager.lookupPercentage(featureId)).isNotEmpty().contains(0.5);
    ddbFeatureLookupManager.deletePercentage(featureId);
    assertThat(ddbFeatureLookupManager.lookupPercentage(featureId)).isEmpty();
  }

}