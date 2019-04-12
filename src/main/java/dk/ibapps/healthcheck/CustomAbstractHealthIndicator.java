package dk.ibapps.healthcheck;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;

import java.util.List;

/**
 * @author Cristian Batusel
 */

abstract class CustomAbstractHealthIndicator extends AbstractHealthIndicator {

	String getAdditionalDetails(List<String> additionalDetails) {
		Gson gson = new GsonBuilder().create();

		return gson.toJson(additionalDetails);
	}
}
