package businessLogics;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Reads brand -> expectedTemplate mapping from JSON file.
 *
 * JSON file path: test/java/resources/brand_template_config.json
 *
 * Example JSON:
 * {
 *   "BWINCOM": "bz_c_desktop_default",
 *   "FOXYGAMES": "fb_c_desktop_default"
 * }
 */
public class BrandConfigUtil {

    private static final Logger log = LogManager.getLogger(BrandConfigUtil.class);

    private static  String CONFIG_PATH;

    private static Map<String, String> expectedTemplates = Collections.emptyMap();

	/*
	 * static { loadConfig(); }
	 */

    public static Map<String, String> loadConfig(String path) {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            CONFIG_PATH = path;
            Reader reader = new FileReader(CONFIG_PATH);
            expectedTemplates = gson.fromJson(reader, type);
            if (expectedTemplates == null) {
                expectedTemplates = Collections.emptyMap();
                return expectedTemplates;
            }
            
//            log.info("Loaded brand template config from JSON. Size: " + expectedTemplates.size());
        } catch (FileNotFoundException e) {
            log.error("Brand template config JSON not found at: " + CONFIG_PATH, e);
            expectedTemplates = Collections.emptyMap();
            return expectedTemplates;
        } catch (Exception e) {
            log.error("Error while loading brand template config JSON", e);
            expectedTemplates = Collections.emptyMap();
            return expectedTemplates;
        }
//        for(map:expectedTemplates);
        return expectedTemplates;
    }

    public static String getExpectedTemplate(String brand) {
        if (brand == null) return null;
        return expectedTemplates.get(brand.toUpperCase());
    }
}