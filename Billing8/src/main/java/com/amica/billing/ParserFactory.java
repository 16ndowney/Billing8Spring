package com.amica.billing;

import static com.amica.billing.Reporter.CONFIGURATION_NAME;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.logging.Level;

import com.amica.acm.configuration.component.ComponentConfigurationsManager;
import com.amica.billing.parse.ApacheCSVParser;
import com.amica.billing.parse.CSVParser;
import com.amica.billing.parse.FlatParser;
import com.amica.billing.parse.JSONParser;
import com.amica.billing.parse.Parser;
import com.amica.escm.configuration.api.Configuration;
import com.amica.escm.configuration.properties.PropertiesConfiguration;
import com.amica.billing.parse.Producer;

import lombok.extern.java.Log;

/**
 * A factory for parsers that determines which type of parser to create
 * based on the extension of given filenames.
 * 
 * @author Will Provost
 */
@Log
public class ParserFactory {

	public static final String PARSER_CLASS_PROPERTY = 
			Reporter.class.getPackage().getName() + ".parserClass";
	
	public static Map<Parser.Format,Supplier<Producer>> parsers = new HashMap<>();
	
	static {
		parsers.put(Parser.Format.CSV, CSVParser::new);
		parsers.put(Parser.Format.FLAT, FlatParser::new);
		parsers.put(Parser.Format.EXPORT, ApacheCSVParser::createExportParser);
		parsers.put(Parser.Format.EXCEL, ApacheCSVParser::createExcelParser);
		parsers.put(Parser.Format.JSON, JSONParser::new);
		parsers.put(Parser.Format.DEFAULT, CSVParser::new);
	}

	/**
	 * Looks up the file extension to find a 
	 * <code>Supplier&lt;Parser&gt;</code>, invokes it, and returns the result. 
	 */
	public static Producer createParser(String filename) {
		int separatorIndex = filename.indexOf(".");
		if (separatorIndex != -1) {
			String extension = filename.substring(separatorIndex + 1);
			for (Parser.Format format : Parser.Format.values()) {
				if (format.toString().equalsIgnoreCase(extension)) {
					return createParser(format);
				}
			}
			log.fine(() -> "Unknown format " + extension + "; using default parser.");
		} else {
			log.fine(() -> "No file extension; using default parser.");
		}
		
		return createParser(Parser.Format.DEFAULT);
	}
	
	/**
	 * Looks up the file extension to find a 
	 * <code>Supplier&lt;Parser&gt;</code>, invokes it, and returns the result. 
	 */
	public static Producer createParser(Parser.Format format) {
		Configuration configuration = new PropertiesConfiguration(new Properties());
		if (System.getProperty("server.env") != null) {
			try {
				configuration = ComponentConfigurationsManager
						.getDefaultComponentConfiguration()
						.getConfiguration(CONFIGURATION_NAME);
			} catch (Throwable ex) {
				log.fine(() -> "No parser configuration found; using default mappings.");
			}
		}
		
		return createParser(configuration, format);
	}
	
	/**
	 * Looks up the file extension to find a 
	 * <code>Supplier&lt;Parser&gt;</code>, invokes it, and returns the result. 
	 */
	public static Producer createParser(Configuration configuration, Parser.Format format) {

		if (configuration.containsKey(PARSER_CLASS_PROPERTY)) {
			String parserClassName = configuration.getString(PARSER_CLASS_PROPERTY);
			try {
				Class<?> parserClass = Class.forName(parserClassName);
				return (Producer) parserClass.newInstance();
			} catch (Exception ex) {
				log.log(Level.WARNING, String.format("%s=%s", 
						PARSER_CLASS_PROPERTY, parserClassName));
				log.log(Level.WARNING, "Couldn't create parser as configured.", ex);
			}
		}

		Supplier<Producer> supplier = parsers.get(format);
		if (supplier != null) {
			return supplier.get();
		}
		
		throw new IllegalArgumentException("No parser configured for " + format);
	}
}
