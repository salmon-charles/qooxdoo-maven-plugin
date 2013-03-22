package org.charless.qxmaven.mojo.qooxdoo.app;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.charless.qxmaven.mojo.qooxdoo.json.Json;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

/**
 * Handles the configuration of a Qooxdoo Application
 * @author charless
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Manifest {
	
	private Map<String,Object> provides = new LinkedHashMap<String, Object>();
	private Map<String,Object> info = new LinkedHashMap<String, Object>();
	
	/**
	 * Read a json file and populate the properties
	 * @param jsonConfig File to read
	 * @return A new instance of Config
	 * @throws Exception
	 */
	public static Manifest read(File jsonConfig) throws Exception {
		ObjectMapper mapper = Json.getMapper();
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS,true);
		Manifest m = mapper.readValue(jsonConfig, Manifest.class);
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS,false);
		return m;
	}
	
	public Manifest copy() throws Exception {
		return Json.getMapper().readValue(Json.getMapper().writeValueAsString(this),this.getClass());
	}
	
	/**
	 * Write the properties into the specified json file
	 * @param jsonConfig File to write
	 * @throws Exception
	 */
	public void write(File jsonConfig) throws Exception {
		ObjectMapper mapper = Json.getMapper();
		ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
		writer.writeValue(jsonConfig, this);
	}

	public Map<String, Object> getProvides() {
		return provides;
	}

	public void setProvides(Map<String, Object> provides) {
		this.provides = provides;
	}
	
	public void providesPut(String key, String value) {
		this.provides.put(key, value);
	}
	
	public String providesGet(String key) {
		return (String)this.provides.get(key);
	}

	public Map<String, Object> getInfo() {
		return info;
	}

	public void setInfo(Map<String, Object> info) {
		this.info = info;
	}
	
	public void infoPut(String key, Object value) {
		this.info.put(key, value);
	}
	
	public Object infoGet(String key) {
		return this.info.get(key);
	}
	
	
	
	
	
}
