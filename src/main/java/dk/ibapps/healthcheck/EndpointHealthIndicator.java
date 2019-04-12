package dk.ibapps.healthcheck;

import org.springframework.boot.actuate.health.Health;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cristian Batusel
 */

public class EndpointHealthIndicator extends CustomAbstractHealthIndicator {
	private String[] endpoints;

	EndpointHealthIndicator(String[] endpoints) {
		this.endpoints = endpoints;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) {
		List<String> messages = new ArrayList<>();
		for (String endpoint : endpoints) {
			try {
				URL url = new URL(endpoint);
				int port = url.getPort();
				if (port == -1) {
					port = url.getDefaultPort();
				}

				try {
					Socket socket = new Socket();
					socket.connect(new InetSocketAddress(url.getHost(), port));
				}
				catch (IOException e) {
					messages.add(endpoint + (" : ") + (e.getMessage()));
				}
			}
			catch (MalformedURLException e1) {
				messages.add(endpoint + (" : ") + (e1.getMessage()));
			}
		}

		if (messages.size() > 0) {
			String additionalDetails = getAdditionalDetails(messages);

			builder.withDetail(HealthEnum.CODE, HealthEnum.URL.getCode());
			builder.withDetail(HealthEnum.MESSAGE, HealthEnum.URL.getMessage());
			builder.withDetail(HealthEnum.ADDITIONAL_DETAILS, additionalDetails);
			builder.down();

			return;
		}
		builder.up();
	}
}
