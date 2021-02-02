package com.amica.billing;

import static com.amica.billing.parse.ParserTestUtility.GOOD_CUSTOMERS;
import static com.amica.billing.parse.ParserTestUtility.GOOD_CUSTOMERS_MAP;
import static com.amica.billing.parse.ParserTestUtility.GOOD_INVOICES;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.amica.billing.parse.Parser;

import com.amica.billing.Reporter.CustomerWithVolume;

public class ReporterTest {

	public static final String CUSTOMER_INPUT = "one\ntwo\nthree";
	public static final String INVOICE_INPUT = "four\nfive";
	
	private StringReader customerReader;
	private StringReader invoiceReader;

	@BeforeClass
	public static void setUpClass() {
		ParserFactory.parsers.put(Parser.Format.DEFAULT, MockParser::new);
	}
	
	
	private Reporter reporter;
	
	@Before
	public void setUp() {
		customerReader = new StringReader(CUSTOMER_INPUT);
		invoiceReader = new StringReader(INVOICE_INPUT);
		
		MockParser.customers = Stream.of(GOOD_CUSTOMERS);
		MockParser.invoices = Stream.of(GOOD_INVOICES);
		
		reporter = new Reporter(customerReader, invoiceReader, 
				Parser.Format.DEFAULT);
	}
	
	@Test
	public void testInitialization() {
		assertThat(MockParser.customerString, equalTo(CUSTOMER_INPUT));
		assertThat(MockParser.invoiceString, equalTo(INVOICE_INPUT));
		assertThat(MockParser.customerMap, equalTo(GOOD_CUSTOMERS_MAP));
	}
	
	@Test
	public void testGetCustomers() {
		assertThat(reporter.getCustomers(), 
				containsInAnyOrder(GOOD_CUSTOMERS));
	}
	
	@Test
	public void testGetInvoices() {
		assertThat(reporter.getInvoices(), 
				containsInAnyOrder(GOOD_INVOICES));
	}
	
	@Test
	public void testReportInvoicesForCustomer() {
		Set<Invoice> invoices = 
				reporter.getInvoicesForCustomer("Customer Two");
		assertThat(invoices, 
				contains(Arrays.copyOfRange(GOOD_INVOICES, 1, 4)));
	}
	
	@Test
	public void testReportInvoicesByCustomer() {
		SortedMap<Customer,SortedSet<Invoice>> invoices = 
				reporter.getInvoicesByCustomer();
		
		assertThat(invoices.get(GOOD_CUSTOMERS[0]), 
				contains(Arrays.copyOfRange(GOOD_INVOICES, 0, 1)));
		assertThat(invoices.get(GOOD_CUSTOMERS[1]), 
				contains(Arrays.copyOfRange(GOOD_INVOICES, 1, 4)));
		assertThat(invoices.get(GOOD_CUSTOMERS[2]), 
				contains(Arrays.copyOfRange(GOOD_INVOICES, 4, 6)));
	}
	
	@Test
	public void testReportOverdueInvoices() {
		Iterator<Invoice> invoices = 
			reporter.getOverdueInvoices(LocalDate.of(2021, 1, 8))
				.iterator();
		assertThat(invoices.next(), equalTo(GOOD_INVOICES[3]));
		assertThat(invoices.next(), equalTo(GOOD_INVOICES[5]));
		assertThat(invoices.next(), equalTo(GOOD_INVOICES[0]));
		assertThat(invoices.hasNext(), equalTo(false));
	}
	
	public static Matcher<CustomerWithVolume> hasNameAndVolume
			(String name, double volume) {
		return allOf(isA(CustomerWithVolume.class),
				hasProperty("customerName", equalTo(name)),
				hasProperty("volume", closeTo(volume, 0.001)));
	}
	@Test
	public void testGetCustomersByVolume() {
		Iterator<CustomerWithVolume> results = 
				reporter.getCustomersByVolume().iterator();
		assertThat(results.next(), hasNameAndVolume
				(GOOD_CUSTOMERS[2].getName(), 1100));
		assertThat(results.next(), hasNameAndVolume
				(GOOD_CUSTOMERS[1].getName(), 900));
		assertThat(results.next(), hasNameAndVolume
				(GOOD_CUSTOMERS[0].getName(), 100));
	}
}
