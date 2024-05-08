package org.codeheadsystems.featureflag.manager.impl;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.waiters.WaiterOverrideConfiguration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

/**
 * The type Ddb control plane.
 */
public class DdbControlPlane {

  private static final Logger LOGGER = LoggerFactory.getLogger(DdbControlPlane.class);

  private final DynamoDbConfiguration dbConfiguration;
  private final DynamoDbClient dbClient;

  /**
   * Instantiates a new Ddb control plane.
   *
   * @param dbConfiguration the db configuration
   * @param dbClient        the db client
   */
  public DdbControlPlane(final DynamoDbConfiguration dbConfiguration, final DynamoDbClient dbClient) {
    LOGGER.info("DdbControlPlane({}, {})", dbConfiguration, dbClient);
    this.dbConfiguration = dbConfiguration;
    this.dbClient = dbClient;
  }

  /**
   * Describe table optional.
   *
   * @return the value if its created.
   */
  private Optional<DescribeTableResponse> describeTable() {
    LOGGER.info("describeTable()");
    try {
      final DescribeTableRequest request = getDescribeTableRequest();
      return Optional.of(dbClient.describeTable(request));
    } catch (ResourceNotFoundException e) {
      LOGGER.info("describeTable() -> not found");
      return Optional.empty();
    }
  }

  private DescribeTableRequest getDescribeTableRequest() {
    return DescribeTableRequest.builder()
        .tableName(dbConfiguration.tableName())
        .build();
  }

  /**
   * Does table exist boolean.
   *
   * @return the boolean
   */
  public boolean doesTableExist() {
    return describeTable().isPresent();
  }

  /**
   * Is table setup correctly boolean.
   *
   * @return the boolean
   */
  public boolean isTableSetupCorrectly() {
    return describeTable().map(response -> {
      final List<KeySchemaElement> keySchema = response.table().keySchema();
      return keySchema.size() == 1
          && keySchema.get(0).attributeName().equals(dbConfiguration.featureHashColumn());
    }).orElse(false);
  }

  /**
   * Sets table.
   */
  public void setupTable() {
    LOGGER.info("setupTable()");
    final Optional<DescribeTableResponse> response = describeTable();
    if (response.isEmpty()) {
      LOGGER.info("Table does not exist, creating");
      final AttributeDefinition hashKeyDefinition = AttributeDefinition.builder()
          .attributeName(dbConfiguration.featureHashColumn())
          .attributeType("S")
          .build();
      final KeySchemaElement hashKey = KeySchemaElement.builder()
          .attributeName(dbConfiguration.featureHashColumn())
          .keyType(KeyType.HASH)
          .build();
      final CreateTableRequest request = CreateTableRequest.builder()
          .tableName(dbConfiguration.tableName())
          .keySchema(List.of(hashKey))
          .attributeDefinitions(List.of(hashKeyDefinition))
          .billingMode(BillingMode.PAY_PER_REQUEST)
          .build();
      dbClient.createTable(request);
      LOGGER.info("Waiting for table to finish creating");
      dbClient.waiter().waitUntilTableExists(
          getDescribeTableRequest(),
          WaiterOverrideConfiguration.builder()
              .waitTimeout(Duration.ofSeconds(5))
              .build());
    } else {
      LOGGER.info("Table already exists");
    }
  }


}
