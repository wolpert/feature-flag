package org.codeheadsystems.featureflag.manager;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeheadsystems.metrics.Metrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricsDecoratorTest {

  @Mock private Metrics metrics;

  @InjectMocks private MetricsDecorator metricsDecorator;

  @Test
  public void testDecorate() {
    FeatureManager.Builder builder = new FeatureManager.Builder();
    builder.withFeatureManagerDecorator(metricsDecorator.featureManagerDecorator());
    builder.withFeatureLookupManagerDecorator(metricsDecorator.featureLookupManagerDecorator());
  }


}