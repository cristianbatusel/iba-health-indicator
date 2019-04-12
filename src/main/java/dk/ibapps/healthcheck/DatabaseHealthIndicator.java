package dk.ibapps.healthcheck;

import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.IncorrectResultSetColumnCountException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static dk.ibapps.healthcheck.HealthEnum.DB;


/**
 * @author Cristian Batusel
 */

public class DatabaseHealthIndicator extends CustomAbstractHealthIndicator {

	private DataSource dataSource;
	private String query;
	private JdbcTemplate jdbcTemplate;

	DatabaseHealthIndicator(DataSource datasource) {
		dataSource = datasource;
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	protected void doHealthCheck(Builder builder) {
		if (this.dataSource == null) {
			builder.up().withDetail("database", "unknown");
		}
		else {
			this.doDataSourceHealthCheck(builder);
		}
	}

	private void doDataSourceHealthCheck(Builder builder) {
		try {
			String product = this.getProduct();
			String validationQuery = this.getValidationQuery(product);
			if (StringUtils.hasText(validationQuery)) {
				List<Object> results = this.jdbcTemplate.query(validationQuery, new DatabaseHealthIndicator.SingleColumnRowMapper());
				DataAccessUtils.requiredSingleResult(results);
			}
		}
		catch (Exception e) {
			String additionalDetails = super.getAdditionalDetails(Collections.singletonList(e.getMessage()));

			builder.withDetail(HealthEnum.CODE, DB.getCode());
			builder.withDetail(HealthEnum.MESSAGE, DB.getMessage());
			builder.withDetail(HealthEnum.ADDITIONAL_DETAILS, additionalDetails);
		}

		builder.up();
	}

	private String getProduct() {
		return this.jdbcTemplate.execute((ConnectionCallback<String>) this::getProduct);
	}

	private String getProduct(Connection connection) throws SQLException {
		return connection.getMetaData().getDatabaseProductName();
	}

	private String getValidationQuery(String product) {
		String query = this.query;
		if (!StringUtils.hasText(query)) {
			DatabaseDriver specific = DatabaseDriver.fromProductName(product);
			query = specific.getValidationQuery();
		}

		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getQuery() {
		return this.query;
	}

	private static class SingleColumnRowMapper implements RowMapper<Object> {
		private SingleColumnRowMapper() {
		}

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			ResultSetMetaData metaData = rs.getMetaData();
			int columns = metaData.getColumnCount();
			if (columns != 1) {
				throw new IncorrectResultSetColumnCountException(1, columns);
			}
			else {
				return JdbcUtils.getResultSetValue(rs, 1);
			}
		}
	}
}
