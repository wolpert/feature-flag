package org.codeheadsystems.featureflag.manager.impl;

import static org.assertj.core.api.Assertions.assertThat;

import io.etcd.jetcd.Client;
import io.etcd.jetcd.test.EtcdClusterExtension;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class EtcdEnablementLookupManagerIntegTest {
  @RegisterExtension
  public static final EtcdClusterExtension cluster = EtcdClusterExtension.builder()
      .withNodes(1)
      .build();
  private Client client;
  private EtcdFeatureLookupManager etcdFeatureLookupManager;
  private String featureId;


  @BeforeEach
  void setupClient() {
    client = Client.builder().endpoints(cluster.clientEndpoints()).build();
    etcdFeatureLookupManager = new EtcdFeatureLookupManager(client, "test");
    featureId = UUID.randomUUID().toString();
  }

  @AfterEach
  void tearDownClient() {
    client.close();
  }

  @Test
  void lookupPercentage_found() {
    etcdFeatureLookupManager.setPercentage(featureId, 0.5);
    Optional<Double> result = etcdFeatureLookupManager.lookupPercentage(featureId);
    assertThat(result).isNotNull()
        .isNotEmpty()
        .contains(0.5);
  }

  @Test
  void lookupPercentage_notFound() {
    Optional<Double> result = etcdFeatureLookupManager.lookupPercentage(featureId);
    assertThat(result).isNotNull()
        .isEmpty();
  }


  @Test
  void deletePercentage() {
    etcdFeatureLookupManager.setPercentage(featureId, 0.5);
    assertThat(etcdFeatureLookupManager.lookupPercentage(featureId)).isNotEmpty().contains(0.5);
    etcdFeatureLookupManager.deletePercentage(featureId);
    assertThat(etcdFeatureLookupManager.lookupPercentage(featureId)).isEmpty();
  }
}
