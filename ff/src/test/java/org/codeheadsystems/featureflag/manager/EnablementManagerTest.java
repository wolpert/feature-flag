package org.codeheadsystems.featureflag.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.codeheadsystems.featureflag.factory.EnablementFactory;
import org.codeheadsystems.featureflag.manager.impl.FeatureManagerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnablementManagerTest {

  private static final String FEATURE_ID = "featureId";
  private static final String DISCRIMINATOR = "discriminator";

  @Mock private FeatureLookupManager featureLookupManager;
  @Mock private EnablementFactory enablementFactory;

  private FeatureManagerImpl featureManager;

  @BeforeEach
  void setUpFeatureManager() {
    featureManager = (FeatureManagerImpl) new FeatureManager.Builder()
        .withFeatureLookupManager(featureLookupManager)
        .withEnablementFactory(enablementFactory)
        .build();
  }

  @Test
  void isEnabled() {
    when(featureLookupManager.lookupPercentage(FEATURE_ID)).thenReturn(Optional.of(0.5));
    when(enablementFactory.generate(0.5)).thenReturn(discriminator -> true);

    assertThat(featureManager.isEnabled(FEATURE_ID, DISCRIMINATOR)).isTrue();
  }

  @Test
  void isEnabled_noFeature() {
    when(featureLookupManager.lookupPercentage(FEATURE_ID)).thenReturn(Optional.empty());
    when(enablementFactory.disabledFeature()).thenReturn(discriminator -> false);

    assertThat(featureManager.isEnabled(FEATURE_ID, DISCRIMINATOR)).isFalse();
  }

  @Test
  void ifEnabledElse_enabled() {
    when(featureLookupManager.lookupPercentage(FEATURE_ID)).thenReturn(Optional.of(0.5));
    when(enablementFactory.generate(0.5)).thenReturn(discriminator -> true);

    assertThat(featureManager.ifEnabledElse(FEATURE_ID, DISCRIMINATOR, () -> "enabled", () -> "disabled"))
        .isEqualTo("enabled");
  }

  @Test
  void ifEnabledElse_disabled() {
    when(featureLookupManager.lookupPercentage(FEATURE_ID)).thenReturn(Optional.empty());
    when(enablementFactory.disabledFeature()).thenReturn(discriminator -> false);

    assertThat(featureManager.ifEnabledElse(FEATURE_ID, DISCRIMINATOR, () -> "enabled", () -> "disabled"))
        .isEqualTo("disabled");
  }

  @Test
  void invalidate() {
    when(featureLookupManager.lookupPercentage(FEATURE_ID)).thenReturn(Optional.empty()).thenReturn(Optional.of(0.5));
    when(enablementFactory.disabledFeature()).thenReturn(discriminator -> false);
    when(enablementFactory.generate(0.5)).thenReturn(discriminator -> true);

    assertThat(featureManager.ifEnabledElse(FEATURE_ID, DISCRIMINATOR, () -> "enabled", () -> "disabled"))
        .isEqualTo("disabled");
    featureManager.invalidate(FEATURE_ID);
    assertThat(featureManager.ifEnabledElse(FEATURE_ID, DISCRIMINATOR, () -> "enabled", () -> "disabled"))
        .isEqualTo("enabled");
    verify(featureLookupManager, times(2)).lookupPercentage(FEATURE_ID);
  }

  @Test
  void invalidate_notCalled() {
    lenient().when(featureLookupManager.lookupPercentage(FEATURE_ID)).thenReturn(Optional.empty()).thenReturn(Optional.of(0.5));
    lenient().when(enablementFactory.disabledFeature()).thenReturn(discriminator -> false);
    lenient().when(enablementFactory.generate(0.5)).thenReturn(discriminator -> true);

    assertThat(featureManager.ifEnabledElse(FEATURE_ID, DISCRIMINATOR, () -> "enabled", () -> "disabled"))
        .isEqualTo("disabled");
    assertThat(featureManager.ifEnabledElse(FEATURE_ID, DISCRIMINATOR, () -> "enabled", () -> "disabled"))
        .isEqualTo("disabled");
    verify(featureLookupManager, times(1)).lookupPercentage(FEATURE_ID);
  }
}