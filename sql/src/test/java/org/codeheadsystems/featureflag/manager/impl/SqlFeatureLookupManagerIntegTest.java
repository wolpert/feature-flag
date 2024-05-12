package org.codeheadsystems.featureflag.manager.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import org.codeheadsystems.featureflag.manager.FeatureLookupManager;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * The type Sql feature lookup manager integ test.
 */
public class SqlFeatureLookupManagerIntegTest extends FeatureLookupManagerIntegTest {

  /**
   * The Jdbi.
   */
  protected Jdbi jdbi;

  /**
   * Sets jdbi.
   */
  @BeforeEach
  void setupJdbi() {
    jdbi = Jdbi.create("jdbc:hsqldb:mem:" + getClass().getSimpleName() + ":" + UUID.randomUUID(), "SA", "");
    jdbi.useHandle(handle -> {
      try (final Connection connection = handle.getConnection()) {
        new LiquibaseHelper().runLiquibase(connection, "feature_flag_liquibase.xml");
      } catch (RuntimeException | SQLException e) {
        throw new IllegalStateException("Database update failure", e);
      }
    });
  }

  /**
   * Close jdbi.
   */
  @AfterEach
  void closeJdbi() {
    jdbi = null;
  }

  @Override
  public FeatureLookupManager manager() {
    return new SqlFeatureLookupManager.Builder().jdbi(jdbi).build();
  }


}