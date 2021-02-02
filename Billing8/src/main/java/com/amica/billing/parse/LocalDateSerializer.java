package com.amica.billing.parse;

import java.io.IOException;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class LocalDateSerializer extends StdSerializer<LocalDate> {

	public LocalDateSerializer() {
		super(LocalDate.class);
	}

	@Override
	public void serialize(LocalDate date, JsonGenerator generator, SerializerProvider provider) throws IOException {
		generator.writeString(date.toString());
		
	}
}
