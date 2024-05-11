package org.codeheadsystems.featureflag.manager.impl;

import io.etcd.jetcd.Client;
import io.etcd.jetcd.test.EtcdClusterExtension;
import org.codeheadsystems.featureflag.manager.FeatureLookupManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * A longer test that starts up an etcd process. But not really that long.
 */
public class EtcdEnablementLookupManagerIntegTest extends FeatureLookupManagerIntegTest {
  @RegisterExtension
  public static final EtcdClusterExtension cluster = EtcdClusterExtension.builder()
      .withNodes(1)
      .build();
  private Client client;

  @BeforeEach
  void setupClient() {
    client = Client.builder().endpoints(cluster.clientEndpoints()).build();
  }

  @AfterEach
  void tearDownClient() {
    client.close();
  }

  @Override
  public FeatureLookupManager manager() {
    return new EtcdFeatureLookupManager(client, "test");
  }

}
