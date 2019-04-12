package dk.ibapps.healthcheck;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityResult;
import org.springframework.boot.actuate.health.Health;

import java.util.Collections;

import static dk.ibapps.healthcheck.HealthEnum.URL;

/**
 * @author Cristian Batusel
 */

public class S3HealthIndicator extends CustomAbstractHealthIndicator {
	private AmazonS3 s3Client;

	S3HealthIndicator(AmazonS3 s3Client) {
		this.s3Client = s3Client;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) {
		StringBuilder sb = new StringBuilder();
		AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard().build();
		GetCallerIdentityResult callerIdResponse = stsClient.getCallerIdentity(new GetCallerIdentityRequest());
		String account = callerIdResponse.getAccount();

		String bucketName = "iba-" + account + "-service-data";

		this.s3Client = AmazonS3ClientBuilder.defaultClient();
		if (!this.s3Client.doesBucketExistV2(bucketName)) {
			sb.append("Could not locate bucket with name ").append(bucketName);
		}

		String message = sb.toString();
		if (!message.isEmpty()) {
			String additionalDetails = getAdditionalDetails(Collections.singletonList(message));

			builder.withDetail(HealthEnum.CODE, URL.getCode());
			builder.withDetail(HealthEnum.MESSAGE, URL.getMessage());
			builder.withDetail(HealthEnum.ADDITIONAL_DETAILS, additionalDetails);
			builder.down();
		}
		builder.up();
	}
}
