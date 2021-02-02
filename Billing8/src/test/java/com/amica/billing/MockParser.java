package com.amica.billing;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amica.billing.parse.Producer;

import lombok.SneakyThrows;

public class MockParser implements Producer {

	public static String customerString;
	public static String invoiceString;
	public static Map<String,Customer> customerMap;
	
	public static Stream<Customer> customers;
	public static Stream<Invoice> invoices;
	
	
	public Stream<Customer> parseCustomers(Reader customerReader) {
		customerString = new BufferedReader(customerReader).lines()
				.collect(Collectors.joining("\n"));
		return customers;
	}
	
	public Stream<Invoice> parseInvoices(Reader invoiceReader, 
			Map<String,Customer> customers) {
		invoiceString = new BufferedReader(invoiceReader).lines()
				.collect(Collectors.joining("\n"));
		customerMap = customers;
		return invoices;
	}

	@SneakyThrows
	public void produceCustomers(Stream<Customer> customers, Writer writer) {
		MockParser.customers = customers;
		writer.write(customerString);
	}

	@SneakyThrows
	public void produceInvoices(Stream<Invoice> invoices, Writer writer) {
		MockParser.invoices = invoices;
		writer.write(invoiceString);
	}
}
