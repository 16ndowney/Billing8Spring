package com.amica.billing;

import static com.amica.billing.ParserFactory.PARSER_CLASS_PROPERTY;
import static com.amica.billing.Reporter.CUSTOMER_FILE_PROPERTY;
import static com.amica.billing.Reporter.INVOICE_FILE_PROPERTY;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;

import org.junit.Test;

import com.amica.billing.Reporter.CustomerWithVolume;
import com.amica.billing.parse.JSONParser;
import com.amica.billing.parse.Parser;
import com.amica.billing.parse.Parser.Format;
import com.amica.escm.configuration.properties.PropertiesConfiguration;

public class ReporterIntegrationTest {

	public static final String INPUT_FOLDER = "src/test/resources/data";
	
	public static void testWithMainDataSet(String suffix,  
			Parser.Format format, Properties properties) throws IOException {
		
		final String CUSTOMER_NAME = "Janis Joplin";
		final String customersFile = INPUT_FOLDER + "/" + "customers" + suffix;
		final String invoicesFile = INPUT_FOLDER + "/" + "invoices" + suffix;

		try (
			FileReader customerReader = suffix != null 
					? new FileReader(customersFile) : null;
			FileReader invoiceReader = suffix != null
					? new FileReader(invoicesFile) : null;
		) {
			Reporter reporter = suffix != null
				? new Reporter(customerReader, invoiceReader, format)
				: ( properties != null
					? new Reporter(new PropertiesConfiguration(properties))
					: new Reporter());
			
			assertThat(reporter.getInvoicesForCustomer(CUSTOMER_NAME),
					hasSize(3));
			
			SortedMap<Customer,SortedSet<Invoice>> invoicesByCustomer =
					reporter.getInvoicesByCustomer();
			assertThat(invoicesByCustomer.keySet(), hasSize(13));
			Customer firstCustomer = invoicesByCustomer.firstKey(); 
			assertThat(firstCustomer.getName(), equalTo("Chet Atkins"));
			assertThat(invoicesByCustomer.get(firstCustomer)
					.first().getNumber(), equalTo(104));
			Customer lastCustomer = invoicesByCustomer.lastKey();
			assertThat(lastCustomer.getName(), equalTo("Lucinda Williams"));
			assertThat(invoicesByCustomer.get(lastCustomer)
					.last().getNumber(),equalTo(112));
			
			SortedSet<Invoice> overdue = reporter.getOverdueInvoices
					(LocalDate.of(2020, 12, 1));
			assertThat(overdue.first().getNumber(), equalTo(102));
			assertThat(overdue.last().getNumber(), equalTo(124));
			
			SortedSet<CustomerWithVolume> customersByVolume =
					reporter.getCustomersByVolume();
			assertThat(customersByVolume.first().getCustomerName(), 
					equalTo("Jerry Reed"));
			assertThat(customersByVolume.last().getCustomerName(), 
					equalTo("Janis Joplin"));
		}
	}
	
	public static void testWithAlternateDataSet(String suffix, 
			Parser.Format format, Properties properties) throws IOException {
		
		final String CUSTOMER_NAME = "Myrna Loy";
		final String customersFile = INPUT_FOLDER + "/" + "customers" + suffix;
		final String invoicesFile = INPUT_FOLDER + "/" + "invoices" + suffix;

		try (
			FileReader customerReader = suffix != null 
					? new FileReader(customersFile) : null;
			FileReader invoiceReader = suffix != null
					? new FileReader(invoicesFile) : null;
		) {
			Reporter reporter = suffix != null
				? new Reporter(customerReader, invoiceReader, format)
				: ( properties != null
					? new Reporter(new PropertiesConfiguration(properties))
					: new Reporter());
			
			assertThat(reporter.getInvoicesForCustomer(CUSTOMER_NAME),
					hasSize(2));
			
			SortedMap<Customer,SortedSet<Invoice>> invoicesByCustomer =
					reporter.getInvoicesByCustomer();
			assertThat(invoicesByCustomer.keySet(), hasSize(13));
			Customer firstCustomer = invoicesByCustomer.firstKey(); 
			assertThat(firstCustomer.getName(), equalTo("Skippy Asta"));
			assertThat(invoicesByCustomer.get(firstCustomer)
					.first().getNumber(), equalTo(550));
			Customer lastCustomer = invoicesByCustomer.lastKey();
			assertThat(lastCustomer.getName(), equalTo("Henry Wadsworth"));
			assertThat(invoicesByCustomer.get(lastCustomer)
					.last().getNumber(),equalTo(957));
			
			SortedSet<Invoice> overdue = reporter.getOverdueInvoices
					(LocalDate.of(2020, 12, 1));
			assertThat(overdue.first().getNumber(), equalTo(746));
			assertThat(overdue.last().getNumber(), equalTo(764));
			
			SortedSet<CustomerWithVolume> customersByVolume =
					reporter.getCustomersByVolume();
			assertThat(customersByVolume.first().getCustomerName(), 
					equalTo("Bert Roach"));
			assertThat(customersByVolume.last().getCustomerName(), 
					equalTo("Skippy Asta"));
		}
	}
	
	@Test
	public void testFromCSV() throws IOException {
		testWithMainDataSet(".csv", Format.CSV, null);
	}
	
	@Test
	public void testFromFlat() throws IOException {
		testWithAlternateDataSet(".flat", Format.FLAT, null);
	}

	@Test
	public void testFromExportCSV() throws IOException {
		testWithMainDataSet("_export.csv", Format.EXPORT, null);
	}

	@Test
	public void testFromExcelCSV() throws IOException {
		testWithMainDataSet("_excel.csv", Format.EXCEL, null);
	}

	@Test
	public void testFromJSON() throws IOException {
		testWithMainDataSet(".json", Format.JSON, null);
	}

	@Test
	public void testFromPrettyJSON() throws IOException {
		testWithMainDataSet("_pretty.json", Format.JSON, null);
	}

	@Test
	public void testFromConfiguredFiles() throws IOException {
		Properties properties = new Properties();
		properties.put(CUSTOMER_FILE_PROPERTY, 
				"src/test/resources/data/customers.csv");
		properties.put(INVOICE_FILE_PROPERTY, 
				"src/test/resources/data/invoices.csv");
		
		testWithMainDataSet(".csv", Format.CSV, properties);
	}

	@Test
	public void testFromConfiguredFilesAndParser() throws IOException {
		Properties properties = new Properties();
		properties.put(CUSTOMER_FILE_PROPERTY, 
				"src/test/resources/data/customers.json");
		properties.put(INVOICE_FILE_PROPERTY, 
				"src/test/resources/data/invoices.json");
		properties.put(PARSER_CLASS_PROPERTY, JSONParser.class.getName());
		
		testWithMainDataSet(".json", Format.JSON, properties);
	}
}
