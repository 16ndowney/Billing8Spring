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

import org.junit.Test;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;

public class ApacheCSVParserTest {

	public static final String EXPORT_CUSTOMER_DATA = 
			"\"First\",\"Last\",\"Terms\"\n" +
			"\"Customer\",\"One\",\"CASH\"\n" +
			"\"Customer\",\"Two\",\"CREDIT_45\"\n" + 
			"\"Customer\",\"Three\",\"CREDIT_30\"\n";
	
	public static final String EXCEL_CUSTOMER_DATA = 
			"Customer,One,CASH\nCustomer,Two,CREDIT_45\nCustomer,Three,CREDIT_30\n";
	
	public static final String EXPORT_INVOICE_DATA = 
			"\"Number\",\"CustomerFirst\",\"CustomerLast\",\"Amount\",\"Date\",\"Paid\"\n" +
			"1,\"Customer\",\"One\",100,\"2021-01-04\",NULL\n" +
			"2,\"Customer\",\"Two\",200,\"2021-01-04\",\"2021-01-05\"\n" +
			"3,\"Customer\",\"Two\",300,\"2021-01-06\",NULL\n" +
			"4,\"Customer\",\"Two\",400,\"2020-11-11\",NULL\n" +
			"5,\"Customer\",\"Three\",500,\"2021-01-04\",\"2021-01-08\"\n" +
			"6,\"Customer\",\"Three\",600,\"2020-12-04\",NULL\n";
	
	public static final String EXCEL_INVOICE_DATA =
			"1,Customer,One,100,2021-01-04,\n" +
			"2,Customer,Two,200,2021-01-04,2021-01-05\n" +
			"3,Customer,Two,300,2021-01-06,\n" +
			"4,Customer,Two,400,2020-11-11,\n" +
			"5,Customer,Three,500,2021-01-04,2021-01-08\n" +
			"6,Customer,Three,600,2020-12-04,\n";
			
	@Test
	public void testParseCustomers_Export() {
		Customer[] customerArray = ApacheCSVParser.createExportParser()
			.parseCustomers(new StringReader(EXPORT_CUSTOMER_DATA))
				.toArray(Customer[]::new);
		assertThat(customerArray, arrayContaining(GOOD_CUSTOMERS));
	}

	@Test
	public void testParseInvoices_Export() {
		Invoice[] invoiceArray = ApacheCSVParser.createExportParser()
			.parseInvoices(new StringReader(EXPORT_INVOICE_DATA), GOOD_CUSTOMERS_MAP)
				.toArray(Invoice[]::new);
		assertThat(invoiceArray, arrayContaining(GOOD_INVOICES));
	}
	
	@Test
	public void testProduceCustomers_Export() {
		StringWriter writer = new StringWriter();
		ApacheCSVParser.createExportParser()
				.produceCustomers(Stream.of(GOOD_CUSTOMERS), writer);
		assertThat(writer.toString(), equalTo(EXPORT_CUSTOMER_DATA));
	}

	@Test
	public void testProduceInvoices_Export() {
		StringWriter writer = new StringWriter();
		ApacheCSVParser.createExportParser()
				.produceInvoices(Stream.of(GOOD_INVOICES), writer);
		assertThat(writer.toString().replace(".0,",  ","), 
				equalTo(EXPORT_INVOICE_DATA));
	}

	@Test
	public void testParseCustomers_Excel() {
		Customer[] customerArray = ApacheCSVParser.createExcelParser()
			.parseCustomers(new StringReader(EXCEL_CUSTOMER_DATA))
				.toArray(Customer[]::new);
		assertThat(customerArray, arrayContaining(GOOD_CUSTOMERS));
	}
	
	@Test
	public void testParseInvoices_Excel() {
		Invoice[] invoiceArray = ApacheCSVParser.createExcelParser()
			.parseInvoices(new StringReader(EXCEL_INVOICE_DATA), GOOD_CUSTOMERS_MAP)
				.toArray(Invoice[]::new);
		assertThat(invoiceArray, arrayContaining(GOOD_INVOICES));
	}
	
	@Test
	public void testProduceCustomers_Excel() {
		StringWriter writer = new StringWriter();
		ApacheCSVParser.createExcelParser()
				.produceCustomers(Stream.of(GOOD_CUSTOMERS), writer);
		assertThat(writer.toString(), equalTo(EXCEL_CUSTOMER_DATA));
	}

	@Test
	public void testProduceInvoices_Excel() {
		StringWriter writer = new StringWriter();
		ApacheCSVParser.createExcelParser()
				.produceInvoices(Stream.of(GOOD_INVOICES), writer);
		assertThat(writer.toString().replace(".0,",  ","), 
				equalTo(EXCEL_INVOICE_DATA));
	}
}
