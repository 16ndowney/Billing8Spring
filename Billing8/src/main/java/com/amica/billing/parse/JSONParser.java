package com.amica.billing.parse;

import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

/**
 * A parser that can read a CSV format with certain expected columns.
 * 
 * @author Will Provost
 */
@Log
public class JSONParser implements Producer {

	ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Consumes the given string streams and translates to {@link Customer}
	 * objects.
	 */
	public Stream<Customer> parseCustomers(Reader customerReader) {
		try {
			return Stream.of(mapper.readValue(customerReader, Customer[].class));
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Couldn't parse customers file.", ex);
		}
		
		return Stream.empty();
	}

	/**
	 * Consumes the given string streams and translates to {@link Invoices}
	 * objects.
	 * 
	 * @param customers
	 *            We use this to translate the customer name to a reference to
	 *            the already-loaded {@link Customer} object.
	 */
	public Stream<Invoice> parseInvoices(Reader invoiceReader, 
			Map<String, Customer> customers) {
		try {
			Invoice[] invoices = mapper.readValue(invoiceReader, Invoice[].class);
			for (Invoice invoice : invoices) {
				invoice.setCustomer(customers.get(invoice.getCustomer().getName()));
			}
			return Stream.of(invoices);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Couldn't parse customers file.", ex);
		}
		
		return Stream.empty();
	}
	
	/**
	 * Use the ObjectMapper to serialize a list that we collect from the stream.
	 */
	@SneakyThrows
	public void produceCustomers(Stream<Customer> customers, Writer writer) {
		mapper.writeValue(writer, customers.collect(Collectors.toList()));
	}

	/**
	 * Use the ObjectMapper to serialize a list that we collect from the stream.
	 */
	@SneakyThrows
	public void produceInvoices(Stream<Invoice> invoices, Writer writer) {
		mapper.writeValue(writer, invoices.collect(Collectors.toList()));
	}
}
