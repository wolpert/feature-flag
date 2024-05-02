package org.codeheadsystems.featureflag.manager.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svarm.common.config.accessor.EtcdAccessor;

@ExtendWith(MockitoExtension.class)
class EtcdEnablementLookupManagerTest {

  private static final String FEATURE_ID = "featureId";

  @Mock private EtcdAccessor accessor;

  @InjectMocks private EtcdFeatureLookupManager etcdFeatureLookupManager;

  @Test
  void lookupPercentage_found() {
    when(accessor.get(EtcdFeatureLookupManager.NAMESPACE, FEATURE_ID)).thenReturn(Optional.of("0.5"));
    Optional<Double> result = etcdFeatureLookupManager.lookupPercentage(FEATURE_ID);
    assertThat(result).isNotNull()
        .isNotEmpty()
        .contains(0.5);
  }

  @Test
  void lookupPercentage_notFound() {
    when(accessor.get(EtcdFeatureLookupManager.NAMESPACE, FEATURE_ID)).thenReturn(Optional.empty());
    Optional<Double> result = etcdFeatureLookupManager.lookupPercentage(FEATURE_ID);
    assertThat(result).isNotNull()
        .isEmpty();
  }

  @Test
  void setPercentage() {
    etcdFeatureLookupManager.setPercentage(FEATURE_ID, 0.5);
    verify(accessor).put(EtcdFeatureLookupManager.NAMESPACE, FEATURE_ID, "0.5");
  }

  @Test
  void deletePercentage() {
    etcdFeatureLookupManager.deletePercentage(FEATURE_ID);
    verify(accessor).delete(EtcdFeatureLookupManager.NAMESPACE, FEATURE_ID);
  }
}