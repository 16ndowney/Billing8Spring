package com.amica.billing;

import static com.amica.billing.ParserFactory.createParser;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import com.amica.billing.parse.Parser;

public class ParserFactoryIntegrationTest {

	@BeforeClass
	public static void setUpClass() {
		System.setProperty("server.env", "ParserFactoryIntegrationTest");
	}
	
	@Test
	public void testCreateParser_MockConfiguration() {
		assertThat(createParser(Parser.Format.JSON), instanceOf(MockParser.class));
	}
}
