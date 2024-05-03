package org.codeheadsystems.featureflag.manager.impl;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.codeheadsystems.featureflag.manager.FeatureLookupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Etcd feature lookup manager.
 */
public class EtcdFeatureLookupManager implements FeatureLookupManager {

  /**
   * The constant NAMESPACE.
   */
  public static final String NAMESPACE = "feature_flag";

  private static final Logger LOGGER = LoggerFactory.getLogger(EtcdFeatureLookupManager.class);

  private final Client client;
  private final String namespaceKeyFormat;

  /**
   * Instantiates a new Etcd feature lookup manager.
   *
   * @param client   for etcd.
   * @param preamble for us to use.
   */
  public EtcdFeatureLookupManager(final Client client,
                                  final String preamble) {
    this.client = client;
    this.namespaceKeyFormat = preamble + "_" + NAMESPACE + "/%s";
    LOGGER.info("EtcdFeatureLookupManager({},{})", namespaceKeyFormat, client);
  }

  @Override
  public Optional<Double> lookupPercentage(final String featureId) {
    LOGGER.trace("lookupPercentage({})", featureId);
    final String namespaceKey = String.format(namespaceKeyFormat, featureId);
    final CompletableFuture<GetResponse> future =
        client.getKVClient().get(ByteSequence.from(namespaceKey.getBytes(StandardCharsets.UTF_8)));
    try {
      final GetResponse getResponse = future.get(100, TimeUnit.MILLISECONDS);
      return getResponse.getKvs().stream()
          .map(KeyValue::getValue)
          .findFirst()
          .map(ByteSequence::toString)
          .map(Double::parseDouble);
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("Unable to get from etcd {}", namespaceKey, e);
      throw new IllegalArgumentException(e);
    } catch (TimeoutException e) {
      LOGGER.info("Not found in etcd {}", namespaceKey);
      return Optional.empty();
    }
  }

  @Override
  public boolean setPercentage(final String featureId, final double percentage) {
    LOGGER.trace("setPercentage({}, {})", featureId, percentage);
    final String namespaceKey = String.format(namespaceKeyFormat, featureId);
    try {
      client.getKVClient().put(
              ByteSequence.from(namespaceKey.getBytes(StandardCharsets.UTF_8)),
              ByteSequence.from(String.valueOf(percentage).getBytes(StandardCharsets.UTF_8)))
          .get();
      return true;
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("Unable to get from etcd {}", namespaceKey, e);
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public void deletePercentage(final String featureId) {
    LOGGER.trace("deletePercentage({})", featureId);
    final String namespaceKey = String.format(namespaceKeyFormat, featureId);
    try {
      client.getKVClient()
          .delete(ByteSequence.from(namespaceKey.getBytes(StandardCharsets.UTF_8)))
          .get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("Unable to delete from etcd {}", namespaceKey, e);
      throw new IllegalArgumentException(e);
    }
  }

}
