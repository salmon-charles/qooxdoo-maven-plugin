package org.charless.qxmaven.mojo.qooxdoo.app;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Untainted;

import org.charless.qxmaven.mojo.qooxdoo.json.Json;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.annotate.JacksonAnnotation;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

/**
 * Handles the configuration of a Qooxdoo Application
 * @author charless
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Config {
	private String name;
	
	@JsonProperty("default-job")
	private String defaultJob = "build";
	
	private Map<String,Object> let = new LinkedHashMap<String, Object>();
	private Map<String,Object> jobs = new LinkedHashMap<String, Object>();
	private ArrayList<LinkedHashMap<String,Object>> include;
	private ArrayList<String> export;
	
	
	/**
	 * Read a json file and populate the properties
	 * @param jsonConfig File to read
	 * @return A new instance of Config
	 * @throws Exception
	 */
	public static Config read(File jsonConfig) throws Exception {
		ObjectMapper mapper = Json.getMapper();
		Config cfg = mapper.readValue(jsonConfig, Config.class);
		return cfg;
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
	
	
	public void addUnitTestJobs() throws Exception {
		this.jobs.put("test",Json.getMapper().readValue(
				"{\"library\" : [ {\"manifest\" : \"./UnitTestManifest.json\"} ]}",Map.class));
		this.jobs.put("test-source",Json.getMapper().readValue(
				"{\"library\" : [ {\"manifest\" : \"./UnitTestManifest.json\"} ]}",Map.class));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@JsonProperty("default-job")
	public String getDefaultJob() {
		return defaultJob;
	}
	
	@JsonProperty("default-job")
	public void setDefaultJob(String defaultJob) {
		this.defaultJob = defaultJob;
	}

	public Map<String, Object> getLet() {
		return let;
	}

	public void setLet(Map<String, Object> let) {
		this.let = let;
	}
	
	public void letPut(String key, Object value) {
		this.let.put(key, value);
	}
	
	public Object letGet(String key) {
		return this.let.get(key);
	}

	public Map<String, Object> getJobs() {
		return jobs;
	}

	public void setJobs(Map<String, Object> jobs) {
		this.jobs = jobs;
	}
	
	public void jobsPut(String key, Object value) {
		this.jobs.put(key, value);
	}
	
	public Object jobsGet(String key) {
		return this.jobs.get(key);
	}


	public ArrayList<LinkedHashMap<String, Object>> getInclude() {
		return include;
	}

	public void setInclude(ArrayList<LinkedHashMap<String, Object>> include) {
		this.include = include;
	}

	public ArrayList<String> getExport() {
		return export;
	}

	public void setExport(ArrayList<String> export) {
		this.export = export;
	}

	
	
}