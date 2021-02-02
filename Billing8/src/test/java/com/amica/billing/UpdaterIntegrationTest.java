package com.amica.billing;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.amica.billing.parse.ApacheCSVParser;
import com.amica.billing.parse.JSONParser;
import com.amica.billing.parse.Parser;
import com.amica.billing.parse.Parser.Format;
import com.amica.escm.configuration.properties.PropertiesConfiguration;

public class UpdaterIntegrationTest {

	public static final String BACKUP_FOLDER = "src/test/resources/data";
	public static final String STAGE_FOLDER = "stage";
	public static final String EXPECTED_FOLDER = "src/test/resources/expected";

	public static final String CUSTOMER_FILE = "customers.json";
	public static final String INVOICE_FILE = "invoices.json";
	
	public static final String RECORDED_DATE = "RECORDED_DATE";
	
	private Updater updaterExplicit;
	private Updater updaterConfigured;
	
	@Before
	public void setUp() throws IOException {
		tearDown();
		
		new File(STAGE_FOLDER).mkdir();
		Files.copy(Paths.get(BACKUP_FOLDER + "/" + CUSTOMER_FILE), 
				Paths.get(STAGE_FOLDER + "/" + CUSTOMER_FILE));
		Files.copy(Paths.get(BACKUP_FOLDER + "/" + INVOICE_FILE), 
				Paths.get(STAGE_FOLDER + "/" + INVOICE_FILE));
			
		updaterExplicit = new Updater
			(STAGE_FOLDER + "/" + CUSTOMER_FILE,
				STAGE_FOLDER + "/" + INVOICE_FILE, Format.JSON);

		Properties properties = new Properties();
		properties.put(Updater.CUSTOMER_FILE_PROPERTY, 
				STAGE_FOLDER + "/" + CUSTOMER_FILE);
		properties.put(Updater.INVOICE_FILE_PROPERTY, 
				STAGE_FOLDER + "/" + INVOICE_FILE);
		properties.put(ParserFactory.PARSER_CLASS_PROPERTY, 
				JSONParser.class.getName());
		updaterConfigured = new Updater
				(new PropertiesConfiguration(properties));
	}
	
	@After
	public void tearDown() throws IOException {
		File outputFolder = new File(STAGE_FOLDER);
		if (outputFolder.exists()) {
			for (File report : outputFolder.listFiles()) {
				report.delete();
			}
			outputFolder.delete();
		}
	}

	private void checkCustomers(String expectedFile) 
			throws IOException {
		try (
			BufferedReader actualReader = new BufferedReader
				(new FileReader(STAGE_FOLDER + "/" + CUSTOMER_FILE));
			BufferedReader expectedReader = new BufferedReader 
				(new FileReader(EXPECTED_FOLDER + "/" + expectedFile));
		) {
			List<Customer> actual = new JSONParser()
					.parseCustomers(actualReader)
					.collect(Collectors.toList());
			Customer[] expected = ApacheCSVParser.createExcelParser()
					.parseCustomers(expectedReader)
					.toArray(Customer[]::new);
			assertThat(actual, containsInAnyOrder(expected));
		}
	}
	
	private void checkInvoices(String expectedFile) 
			throws IOException {
		try (
			BufferedReader customersReader = new BufferedReader
				(new FileReader(STAGE_FOLDER + "/" + CUSTOMER_FILE));
			BufferedReader actualReader = new BufferedReader
						(new FileReader(STAGE_FOLDER + "/" + INVOICE_FILE));
			BufferedReader expectedReader = new BufferedReader 
				(new FileReader(EXPECTED_FOLDER + "/" + expectedFile));
		) {
			String invoices = expectedReader.lines().collect
					(Collectors.joining("\n"));
			StringReader invoicesReader = new StringReader
				(invoices.replace(RECORDED_DATE, 
					LocalDate.now().toString()));
			
			Parser parser = new JSONParser();
			Map<String,Customer> customers = parser.parseCustomers
				(customersReader).collect(Collectors.toConcurrentMap
					(Customer::getName, Function.identity()));
			List<Invoice> actual = parser.parseInvoices
					(actualReader, customers).collect(Collectors.toList());
			Invoice[] expected = ApacheCSVParser.createExcelParser()
					.parseInvoices(invoicesReader, customers)
					.toArray(Invoice[]::new);
			assertThat(actual, containsInAnyOrder(expected));
		}
	}
	
	@Test
	public void testCreateCustomerExplicit() throws IOException {
		updaterExplicit.createCustomer("Merle", "Haggard", Terms.CASH);
		updaterExplicit.save();
		checkCustomers("new_customer.csv");
	}
	
	@Test
	public void testCreateInvoiceExplicit() throws IOException {
		updaterExplicit.createInvoice("Chet Atkins", 777);
		updaterExplicit.save();
		checkInvoices("new_invoice.csv");
	}
	
	@Test
	public void testPayInvoiceExplicit() throws IOException {
		updaterExplicit.payInvoice(107);
		updaterExplicit.save();
		checkInvoices("paid_invoice.csv");
	}

	@Test
	public void testCreateCustomerConfigured() throws IOException {
		updaterConfigured.createCustomer("Merle", "Haggard", Terms.CASH);
		updaterConfigured.save();
		checkCustomers("new_customer.csv");
	}

	@Test
	public void testCreateInvoiceConfigured() throws IOException {
		updaterConfigured.createInvoice("Chet Atkins", 777);
		updaterConfigured.save();
		checkInvoices("new_invoice.csv");
	}
	
	@Test
	public void testPayInvoiceConfigured() throws IOException {
		updaterConfigured.payInvoice(107);
		updaterConfigured.save();
		checkInvoices("paid_invoice.csv");
	}
}
