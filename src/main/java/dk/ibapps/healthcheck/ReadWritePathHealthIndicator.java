package dk.ibapps.healthcheck;

import org.springframework.boot.actuate.health.Health;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static dk.ibapps.healthcheck.HealthEnum.RW;

/**
 * @author Cristian Batusel
 */

public class ReadWritePathHealthIndicator extends CustomAbstractHealthIndicator {
	private String[] readPaths;
	private String[] writePaths;

	ReadWritePathHealthIndicator(String[] readPaths, String[] writePaths) {
		this.readPaths = readPaths;
		this.writePaths = writePaths;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) {
		List<String> messages = new ArrayList<>();
		if (readPaths != null) {
			for (String path : readPaths) {
				boolean readable = Files.isReadable(Paths.get(path));

				if (!readable) {
					messages.add((path) + (" is not readable"));
				}
			}
		}
		if (writePaths != null) {
			for (String path : writePaths) {
				boolean writable = Files.isWritable(Paths.get(path));
				if (!writable) {
					messages.add((path) + (" is not writable"));
				}
			}
		}
		if (messages.size() > 0) {
			String additionalDetails = super.getAdditionalDetails(messages);

			builder.withDetail(HealthEnum.CODE, RW.getCode());
			builder.withDetail(HealthEnum.MESSAGE, RW.getMessage());
			builder.withDetail(HealthEnum.ADDITIONAL_DETAILS, additionalDetails);

			builder.down();

			return;
		}
		builder.up();
	}

}
