package org.charless.qxmaven.mojo.qooxdoo.json;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class Json {
	private static ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally

	public static ObjectMapper getMapper() {
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES,true);
 		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS,true);
		return mapper;
	}
	
}
