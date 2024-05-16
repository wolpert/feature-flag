package org.codeheadsystems.featureflag.manager.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.codeheadsystems.featureflag.manager.impl.EtcdFeatureLookupManager.NAMESPACE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EtcdEnablementLookupManagerTest {

  private static final String FEATURE_ID = "featureId";

  private static final String PREAMBLE = "p";

  @Mock private Client client;
  @Mock private KV kv;
  @Mock private KeyValue keyValue;
  @Mock private GetResponse getResponse;
  @Mock private CompletableFuture<PutResponse> putResponseCompletableFuture;
  @Mock private CompletableFuture<DeleteResponse> deleteResponseCompletableFuture;
  @Mock private CompletableFuture<GetResponse> getResponseCompletableFuture;

  @Captor private ArgumentCaptor<ByteSequence> byteSequenceArgumentCaptor;

  private EtcdFeatureLookupManager etcdFeatureLookupManager;

  @NotNull
  private static ByteSequence getNamespaceKeyBytes() {
    final String namespaceKey = PREAMBLE + "_" + NAMESPACE + "/" + FEATURE_ID;
    return ByteSequence.from(namespaceKey.getBytes());
  }


  @BeforeEach
  void setUp() {
    etcdFeatureLookupManager = new EtcdFeatureLookupManager(client, PREAMBLE);
  }

  @Test
  void lookupPercentage_found() throws ExecutionException, InterruptedException, TimeoutException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.get(getNamespaceKeyBytes())).thenReturn(getResponseCompletableFuture);
    when(getResponseCompletableFuture.get(100, TimeUnit.MILLISECONDS)).thenReturn(getResponse);
    when(getResponse.getKvs()).thenReturn(List.of(keyValue));
    when(keyValue.getValue()).thenReturn(ByteSequence.from("0.5".getBytes()));
    Optional<Double> result = etcdFeatureLookupManager.lookupPercentage(FEATURE_ID);
    assertThat(result).isNotNull()
        .isNotEmpty()
        .contains(0.5);
  }

  @Test
  void lookupPercentage_notFound() throws ExecutionException, InterruptedException, TimeoutException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.get(getNamespaceKeyBytes())).thenReturn(getResponseCompletableFuture);
    when(getResponseCompletableFuture.get(100, TimeUnit.MILLISECONDS)).thenReturn(getResponse);
    when(getResponse.getKvs()).thenReturn(List.of());
    Optional<Double> result = etcdFeatureLookupManager.lookupPercentage(FEATURE_ID);
    assertThat(result).isNotNull()
        .isEmpty();
  }

  @Test
  void lookupPercentage_interrupted() throws ExecutionException, InterruptedException, TimeoutException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.get(getNamespaceKeyBytes())).thenReturn(getResponseCompletableFuture);
    when(getResponseCompletableFuture.get(100, TimeUnit.MILLISECONDS)).thenThrow(new InterruptedException());
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> etcdFeatureLookupManager.lookupPercentage(FEATURE_ID));
  }

  @Test
  void lookupPercentage_executionException() throws ExecutionException, InterruptedException, TimeoutException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.get(getNamespaceKeyBytes())).thenReturn(getResponseCompletableFuture);
    when(getResponseCompletableFuture.get(100, TimeUnit.MILLISECONDS)).thenThrow(new ExecutionException("", new Exception()));
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> etcdFeatureLookupManager.lookupPercentage(FEATURE_ID));
  }

  @Test
  void setPercentage() throws ExecutionException, InterruptedException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.put(byteSequenceArgumentCaptor.capture(), byteSequenceArgumentCaptor.capture())).thenReturn(putResponseCompletableFuture);
    etcdFeatureLookupManager.setPercentage(FEATURE_ID, 0.5);
    verify(putResponseCompletableFuture).get();
    List<String> values = byteSequenceArgumentCaptor.getAllValues().stream().map(Objects::toString).collect(Collectors.toList());
    assertThat(values).containsExactly(PREAMBLE + "_" + NAMESPACE + "/" + FEATURE_ID, "0.5");
    verify(putResponseCompletableFuture).get();
  }

  @Test
  void setPercentage_interrupted() throws ExecutionException, InterruptedException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.put(byteSequenceArgumentCaptor.capture(), byteSequenceArgumentCaptor.capture())).thenReturn(putResponseCompletableFuture);
    when(putResponseCompletableFuture.get()).thenThrow(new InterruptedException());
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> etcdFeatureLookupManager.setPercentage(FEATURE_ID, 0.5));
  }

  @Test
  void setPercentage_executionException() throws ExecutionException, InterruptedException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.put(byteSequenceArgumentCaptor.capture(), byteSequenceArgumentCaptor.capture())).thenReturn(putResponseCompletableFuture);
    when(putResponseCompletableFuture.get()).thenThrow(new ExecutionException("", new Exception()));
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> etcdFeatureLookupManager.setPercentage(FEATURE_ID, 0.5));
  }

  @Test
  void deletePercentage() throws ExecutionException, InterruptedException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.delete(byteSequenceArgumentCaptor.capture())).thenReturn(deleteResponseCompletableFuture);
    etcdFeatureLookupManager.deletePercentage(FEATURE_ID);
    verify(deleteResponseCompletableFuture).get();
    List<String> values = byteSequenceArgumentCaptor.getAllValues().stream().map(Objects::toString).collect(Collectors.toList());
    assertThat(values).containsExactly(PREAMBLE + "_" + NAMESPACE + "/" + FEATURE_ID);
  }

  @Test
  void deletePercentage_interrupted() throws ExecutionException, InterruptedException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.delete(byteSequenceArgumentCaptor.capture())).thenReturn(deleteResponseCompletableFuture);
    when(deleteResponseCompletableFuture.get()).thenThrow(new InterruptedException());
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> etcdFeatureLookupManager.deletePercentage(FEATURE_ID));
  }

  @Test
  void deletePercentage_executionException() throws ExecutionException, InterruptedException {
    when(client.getKVClient()).thenReturn(kv);
    when(kv.delete(byteSequenceArgumentCaptor.capture())).thenReturn(deleteResponseCompletableFuture);
    when(deleteResponseCompletableFuture.get()).thenThrow(new ExecutionException("", new Exception()));
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> etcdFeatureLookupManager.deletePercentage(FEATURE_ID));
  }

}