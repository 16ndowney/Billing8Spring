package com.amica.billing.parse;

import java.io.IOException;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class LocalDateDeserializer extends StdDeserializer<LocalDate> {
	
	public LocalDateDeserializer() {
		super(LocalDate.class);
	}

	public LocalDate deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		return LocalDate.parse(parser.getValueAsString());
	}

}
