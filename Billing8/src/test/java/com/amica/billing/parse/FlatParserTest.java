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
import org.junit.Test;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;

public class FlatParserTest {

	public static final String GOOD_CUSTOMER_DATA = 
			"Customer    One         CASH      \n" +
			"Customer    Two         CREDIT_45 \n" +
			"Customer    Three       CREDIT_30 \n";
	
	public static final String BAD_CUSTOMER_DATA =
			"Customer    One         CASHY     \n" +
			"Customer    Two\n" +
			"Customer    Three       CREDIT_30 \n";

	public static final String GOOD_INVOICE_DATA = 
			"   1Customer    One           100.00010421      \n" +
			"   2Customer    Two           200.00010421010521\n" +
			"   3Customer    Two           300.00010621      \n" +
			"   4Customer    Two           400.00111120      \n" +
			"   5Customer    Three         500.00010421010821\n" +
			"   6Customer    Three         600.00120420      \n";
	
	public static final String BAD_INVOICE_DATA = 
			"   1Customer    One              100010421      \n" +
			"   2Customer    Two              200010421010521\n" +
			"   3Customer    Two\n" +
			"   4Customer    Two            400.0993020      \n" +
			"   5Customer    Four             500010421010821\n" +
			"   6Customer    Three            600120420      \n";
	
	private Producer parser = new FlatParser();
	
	@SuppressWarnings("unchecked")
	private <T> Matcher<T>[] matchersFor(T[] expectedObjects) {
		return Stream.of(expectedObjects)
				.map(Matchers::samePropertyValuesAs)
				.toArray(Matcher[]::new);
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
		assertThat(writer.toString(), equalTo(GOOD_INVOICE_DATA));
	}
}
