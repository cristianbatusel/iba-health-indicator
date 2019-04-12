package dk.ibapps.healthcheck;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.CompositeHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.OrderedHealthAggregator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * @author Cristian Batusel
 */

@Component
public class GenericHealthIndicator implements HealthIndicator {

	private DataSource dataSource;

	private AmazonS3 amazonS3;

	@Value("${healthCheck.database}")
	private boolean checkDatabase;

	@Value("${healthCheck.s3}")
	private boolean checkS3;

	@Value("${healthCheck.endpoints}")
	private String[] endpoints;

	@Value("${healthCheck.readPaths}")
	private String[] readPaths;

	@Value("${healthCheck.writePaths}")
	private String[] writePaths;

	public Health health() {
		HealthAggregator healthAggregator = new OrderedHealthAggregator();
		CompositeHealthIndicator compositeHealthIndicator = new CompositeHealthIndicator(healthAggregator);
		if (checkDatabase) {
			DatabaseHealthIndicator dataSourceHealthIndicator = new DatabaseHealthIndicator(dataSource);
			dataSourceHealthIndicator.setQuery("SELECT 1 FROM DUAL");
			compositeHealthIndicator.addHealthIndicator("db", dataSourceHealthIndicator);
		}
		if ((readPaths != null && readPaths.length > 0) || (writePaths != null && writePaths.length > 0)) {
			ReadWritePathHealthIndicator readWritePathHealthIndicator = new ReadWritePathHealthIndicator(readPaths, writePaths);
			compositeHealthIndicator.addHealthIndicator("readWrite", readWritePathHealthIndicator);
		}
		if (endpoints != null && endpoints.length > 0) {
			EndpointHealthIndicator endpointHealthIndicator = new EndpointHealthIndicator(endpoints);
			compositeHealthIndicator.addHealthIndicator("thirdParty", endpointHealthIndicator);
		}
		if (checkS3) {
			S3HealthIndicator s3HealthIndicator = new S3HealthIndicator(amazonS3);
			compositeHealthIndicator.addHealthIndicator("S3", s3HealthIndicator);
		}
		return compositeHealthIndicator.health();
	}

	@Autowired(required = false)
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Autowired(required = false)
	public void setAmazonS3(AmazonS3 amazonS3) {
		this.amazonS3 = amazonS3;
	}
}
