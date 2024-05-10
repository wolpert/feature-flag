package org.codeheadsystems.featureflag.manager.impl;

import java.util.Map;
import java.util.Optional;
import org.codeheadsystems.featureflag.manager.FeatureLookupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

/**
 * The type Ddb feature lookup manager.
 */
public class DdbFeatureLookupManager implements FeatureLookupManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(DdbFeatureLookupManager.class);

  private final DynamoDbConfiguration dbConfiguration;
  private final DynamoDbClient dbClient;

  /**
   * Instantiates a new Ddb feature lookup manager.
   *
   * @param dbConfiguration the db configuration
   * @param dbClient        the db client
   */
  public DdbFeatureLookupManager(final DynamoDbConfiguration dbConfiguration,
                                 final DynamoDbClient dbClient) {
    LOGGER.info("DdbFeatureLookupManager({}, {})", dbConfiguration, dbClient);
    this.dbConfiguration = dbConfiguration;
    this.dbClient = dbClient;
  }

  @Override
  public Optional<Double> lookupPercentage(final String featureId) {
    LOGGER.trace("lookupPercentage({})", featureId);
    final GetItemRequest request = GetItemRequest.builder()
        .key(getHashLookup(featureId))
        .attributesToGet(dbConfiguration.percentageColumn())
        .tableName(dbConfiguration.tableName())
        .build();
    final Map<String, AttributeValue> returnedItem = dbClient.getItem(request).item();
    if (returnedItem.containsKey(dbConfiguration.percentageColumn())) {
      return Optional.of(Double.parseDouble(returnedItem.get(dbConfiguration.percentageColumn()).n()));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public boolean setPercentage(final String featureId, final double percentage) {
    LOGGER.trace("setPercentage({}, {})", featureId, percentage);
    final PutItemRequest request = PutItemRequest.builder()
        .item(Map.of(
            dbConfiguration.featureHashColumn(),
            AttributeValue.builder().s(featureId).build(),
            dbConfiguration.percentageColumn(),
            AttributeValue.builder().n(Double.toString(percentage)).build()))
        .tableName(dbConfiguration.tableName())
        .build();
    dbClient.putItem(request);
    return true;
  }

  @Override
  public void deletePercentage(final String featureId) {
    LOGGER.trace("deletePercentage({})", featureId);
    final DeleteItemRequest request = DeleteItemRequest.builder()
        .key(getHashLookup(featureId))
        .tableName(dbConfiguration.tableName())
        .build();
    dbClient.deleteItem(request);
  }

  private Map<String, AttributeValue> getHashLookup(final String featureId) {
    LOGGER.trace("getHashLookup({})", featureId);
    return Map.of(
        dbConfiguration.featureHashColumn(),
        AttributeValue.builder().s(featureId).build());
  }
}
