package com.amica.billing.parse;

import static com.amica.billing.parse.ParserTestUtility.BAD_CUSTOMERS;
import static com.amica.billing.parse.ParserTestUtility.BAD_INVOICES;
import static com.amica.billing.parse.ParserTestUtility.GOOD_CUSTOMERS;
import static com.amica.billing.parse.ParserTestUtility.GOOD_CUSTOMERS_MAP;
import static com.amica.billing.parse.ParserTestUtility.GOOD_INVOICES;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;

public class CSVParserTest {

	public static final String GOOD_CUSTOMER_DATA = 
			"Customer,One,CASH\nCustomer,Two,45\nCustomer,Three,30\n";
	
	public static final String BAD_CUSTOMER_DATA = 
			"Customer,One,CASHY_MONEY\nCustomer,Two\nCustomer,Three,30\n";

	public static final String GOOD_INVOICE_DATA = 
			"1,Customer,One,100,2021-01-04\n" +
			"2,Customer,Two,200,2021-01-04,2021-01-05\n" +
			"3,Customer,Two,300,2021-01-06\n" +
			"4,Customer,Two,400,2020-11-11\n" +
			"5,Customer,Three,500,2021-01-04,2021-01-08\n" +
			"6,Customer,Three,600,2020-12-04\n";
	
	public static final String BAD_INVOICE_DATA = 
			"1,Customer,One,100,2021-01-04\n" +
			"2,Customer,Two,200,2021-01-04,2021-01-05\n" +
			"3,Customer,Two,300\n" +
			"4,Customer,Four,400,2020-11-11\n" +
			"5,Customer,Three,500,2021-01-04,20210108\n" +
			"6,Customer,Three,600,2020-12-04\n";
	
	protected Producer parser;
	
	@SuppressWarnings("unchecked")
	private <T> Matcher<T>[] matchersFor(T[] expectedObjects) {
		return Stream.of(expectedObjects)
				.map(Matchers::samePropertyValuesAs)
				.toArray(Matcher[]::new);
	}
	
	@Before
	public void setUp() {
		parser = new CSVParser();
	}
	
	@Test
	public void testParseCustomers() {
		Customer[] customerArray = parser.parseCustomers
			(new StringReader(GOOD_CUSTOMER_DATA))
				.toArray(Customer[]::new);
		assertThat(customerArray, arrayContaining(matchersFor(GOOD_CUSTOMERS)));
	}
	
	@Test
	public void testParseCustomers_Bad() {
		Customer[] customerArray = parser.parseCustomers
			(new StringReader(BAD_CUSTOMER_DATA))
				.toArray(Customer[]::new);
		assertThat(customerArray, arrayContaining(matchersFor(BAD_CUSTOMERS)));
	}
	
	@Test
	public void testParseInvoices() {
		Invoice[] invoiceArray = parser.parseInvoices
			(new StringReader(GOOD_INVOICE_DATA), GOOD_CUSTOMERS_MAP)
				.toArray(Invoice[]::new);
		assertThat(invoiceArray, arrayContaining(matchersFor(GOOD_INVOICES)));
	}
	
	@Test
	public void testParseInvoices_Bad() {
		Invoice[] invoiceArray = parser.parseInvoices
			(new StringReader(BAD_INVOICE_DATA), GOOD_CUSTOMERS_MAP)
				.toArray(Invoice[]::new);
		assertThat(invoiceArray, arrayContaining(matchersFor(BAD_INVOICES)));
	}
	
	@Test
	public void testProduceCustomers() {
		StringWriter writer = new StringWriter();
		parser.produceCustomers(Stream.of(GOOD_CUSTOMERS), writer);
		assertThat(writer.toString(), equalTo(GOOD_CUSTOMER_DATA));
	}
	
	@Test
	public void testProduceInvoices() {
		StringWriter writer = new StringWriter();
		parser.produceInvoices(Stream.of(GOOD_INVOICES), writer);
		assertThat(writer.toString().replace(".00",  ""), 
				equalTo(GOOD_INVOICE_DATA));
	}
}
