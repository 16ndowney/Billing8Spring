package com.amica.billing.parse;


import static com.amica.billing.parse.ParserTestUtility.GOOD_CUSTOMERS;
import static com.amica.billing.parse.ParserTestUtility.GOOD_CUSTOMERS_MAP;
import static com.amica.billing.parse.ParserTestUtility.GOOD_INVOICES;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;

public class JSONParserTest {

	public static final String GOOD_CUSTOMER_DATA = 
			"[{\"firstName\":\"Customer\",\"lastName\":\"One\",\"terms\":\"CASH\"}," +
			 "{\"firstName\":\"Customer\",\"lastName\":\"Two\",\"terms\":\"CREDIT_45\"}," +
			 "{\"firstName\":\"Customer\",\"lastName\":\"Three\",\"terms\":\"CREDIT_30\"}]";
	
	public static final String GOOD_INVOICE_DATA =
			"[{\"number\":1,\"customer\":{\"firstName\":\"Customer\",\"lastName\":\"One\",\"terms\":\"CASH\"},\"amount\":100.0,\"theDate\":\"2021-01-04\",\"paidDate\":null}," +
			 "{\"number\":2,\"customer\":{\"firstName\":\"Customer\",\"lastName\":\"Two\",\"terms\":\"CREDIT_45\"},\"amount\":200.0,\"theDate\":\"2021-01-04\",\"paidDate\":\"2021-01-05\"}," +
			 "{\"number\":3,\"customer\":{\"firstName\":\"Customer\",\"lastName\":\"Two\",\"terms\":\"CREDIT_45\"},\"amount\":300.0,\"theDate\":\"2021-01-06\",\"paidDate\":null}," +
			 "{\"number\":4,\"customer\":{\"firstName\":\"Customer\",\"lastName\":\"Two\",\"terms\":\"CREDIT_45\"},\"amount\":400.0,\"theDate\":\"2020-11-11\",\"paidDate\":null}," +
			 "{\"number\":5,\"customer\":{\"firstName\":\"Customer\",\"lastName\":\"Three\",\"terms\":\"CREDIT_30\"},\"amount\":500.0,\"theDate\":\"2021-01-04\",\"paidDate\":\"2021-01-08\"}," +
			 "{\"number\":6,\"customer\":{\"firstName\":\"Customer\",\"lastName\":\"Three\",\"terms\":\"CREDIT_30\"},\"amount\":600.0,\"theDate\":\"2020-12-04\",\"paidDate\":null}]";
	
	protected Producer parser;
	
	@Before
	public void setUp() {
		parser = new JSONParser();
	}
	
	@Test
	public void testParseCustomers() {
		Customer[] customerArray = parser.parseCustomers
			(new StringReader(GOOD_CUSTOMER_DATA))
				.toArray(Customer[]::new);
		assertThat(customerArray, arrayContaining(GOOD_CUSTOMERS));
	}
	
	@Test
	public void testParseInvoices() {
		Invoice[] invoiceArray = parser.parseInvoices
			(new StringReader(GOOD_INVOICE_DATA), GOOD_CUSTOMERS_MAP)
				.toArray(Invoice[]::new);
		assertThat(invoiceArray, arrayContaining(GOOD_INVOICES));
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
