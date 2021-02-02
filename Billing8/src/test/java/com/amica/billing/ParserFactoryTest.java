package com.amica.billing;

import static com.amica.billing.ParserFactory.createParser;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;
import org.junit.Before;
import org.junit.Test;

import com.amica.billing.parse.ApacheCSVParser;
import com.amica.billing.parse.CSVParser;
import com.amica.billing.parse.FlatParser;
import com.amica.billing.parse.JSONParser;
import com.amica.billing.parse.Parser;
import com.amica.escm.configuration.properties.PropertiesConfiguration;

public class ParserFactoryTest {

	@Before
	public void setUp() {
		ParserFactory.parsers.put(Parser.Format.CSV, CSVParser::new);
	}
	
	@Test
	public void testCreateParser_CSVFilename() {
		assertThat(createParser("any.csv"), instanceOf(CSVParser.class));
	}
	
	@Test
	public void testCreateParser_CSVFormat() {
		assertThat(createParser(Parser.Format.CSV), instanceOf(CSVParser.class));
	}
	
	@Test
	public void testCreateParser_FlatFilename () {
		assertThat(createParser("any.flat"), instanceOf(FlatParser.class));
	}
	
	@Test
	public void testCreateParser_FlatFormat () {
		assertThat(createParser(Parser.Format.FLAT), instanceOf(FlatParser.class));
	}
	
	@Test
	public void testCreateParser_UnknownExtension() {
		assertThat(createParser("x.y.z"), instanceOf(CSVParser.class));
	}
	
	@Test
	public void testCreateParser_ExportFormat() {
		Parser parser = createParser(Parser.Format.EXPORT); 
		assertThat(parser, instanceOf(ApacheCSVParser.class));
		CSVFormat csvFormat = ((ApacheCSVParser) parser).getCSVFormat();
		assertThat(csvFormat.getQuoteMode(), equalTo(QuoteMode.NON_NUMERIC));
		assertThat(csvFormat.getNullString(), equalTo("NULL"));
	}
	
	@Test
	public void testCreateParser_ExcelFormat() {
		Parser parser = createParser(Parser.Format.EXCEL); 
		assertThat(parser, instanceOf(ApacheCSVParser.class));
		CSVFormat csvFormat = ((ApacheCSVParser) parser).getCSVFormat();
		assertThat(csvFormat.getNullString(), equalTo(""));
	}
	
	@Test
	public void testCreateParser_JSONFormat() {
		assertThat(createParser(Parser.Format.JSON), instanceOf(JSONParser.class));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateParser_NullFormat() {
		Parser.Format format = null;
		createParser(format);
	}
	
	@Test
	public void testCreateParser_DefaultFormat () {
		assertThat(createParser(Parser.Format.DEFAULT), instanceOf(CSVParser.class));
	}
	
	@Test
	public void testCreateParser_Overridden() {
		ParserFactory.parsers.put(Parser.Format.CSV, FlatParser::new);
		assertThat(createParser("any.csv"), instanceOf(FlatParser.class));
	}
	
	@Test
	public void testCreateParser_MockPropertiesConfiguration() {
		Properties properties = new Properties();
		properties.put(ParserFactory.PARSER_CLASS_PROPERTY, 
				MockParser.class.getName());
		assertThat(createParser(new PropertiesConfiguration(properties), 
				Parser.Format.JSON), instanceOf(MockParser.class));
	}
}
