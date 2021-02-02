package com.amica.billing;

import static java.util.function.Function.identity;

import java.io.FileReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.amica.acm.configuration.component.ComponentConfigurationsManager;
import com.amica.billing.parse.Parser;
import com.amica.escm.configuration.api.Configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

/**
 * This is the central component of the system.
 * It reads a file of {@link Customer}s and a file of {@link Invoice}s,
 * using configurable {@link Parser}s so as to to handle different file 
 * formats; and then can produce reports based on a few different queries
 * and relying on a generic {@link TextReporter report generator}. 
 * 
 * @author Will Provost
 */
@Log
public class Reporter {

	public static final String CONFIGURATION_NAME = "Billing";
	public static final String CUSTOMER_FILE_PROPERTY =
			Reporter.class.getPackage().getName() + ".customerFile";
	public static final String INVOICE_FILE_PROPERTY =
			Reporter.class.getPackage().getName() + ".invoiceFile";
	
	public static int compareByName(Customer a, Customer b) {
		return (a.getLastName() + a.getFirstName()).compareTo
				(b.getLastName() + b.getFirstName());
	}
	
	public static int compareByNumber(Invoice a, Invoice b) {
		return Integer.compare(a.getNumber(), b.getNumber());
	}
	
	public static int compareByDate(Invoice a, Invoice b) {
		return a.getTheDate().compareTo(b.getTheDate());
	}
	
	private Map<String,Customer> customers;
	private List<Invoice> invoices;
	
	/**
	 * Customer and invoice data is found in files whose names are provided
	 * using the configuration manager.
	 */
	public Reporter(Configuration configuration) {
		
		String customerFile = configuration.getString(CUSTOMER_FILE_PROPERTY);
		String invoiceFile = configuration.getString(INVOICE_FILE_PROPERTY);
		
		try (
			FileReader customerReader = new FileReader(customerFile);
			FileReader invoiceReader = new FileReader(invoiceFile);
		) {
			readData(customerReader, invoiceReader, 
				ParserFactory.createParser(configuration, 
					Parser.Format.DEFAULT));
		} catch (Exception ex) {
			log.log(Level.SEVERE, String.format("%s=%s", 
					CUSTOMER_FILE_PROPERTY, customerFile));
			log.log(Level.SEVERE, String.format("%s=%s", 
					INVOICE_FILE_PROPERTY, invoiceFile));
			log.log(Level.SEVERE, "Couldn't load files as configured", ex);
		}
	}

		/**
	 * Customer and invoice data is found in files whose names are provided
	 * using the configuration manager.
	 */
	public Reporter() {
		this(ComponentConfigurationsManager.getDefaultComponentConfiguration()
				.getConfiguration(CONFIGURATION_NAME));
	}

	/**
	 * Provide readers with customer and invoice data, and the data format. 
	 * The invoice data is expected to include customer names,
	 * and in loading the data we re-connect the invoices so that they refer
	 * directly to the customer objects in memory.
	 */
	public Reporter(Reader customerReader, Reader invoiceReader) {
		this(customerReader, invoiceReader, Parser.Format.DEFAULT);
	}

	/**
	 * Provide the locations of a file of customer data and a file of 
	 * invoice data. The invoice data is expected to include customer names,
	 * and in loading the data we re-connect the invoices so that they refer
	 * directly to the customer objects in memory.
	 */
	public Reporter(Reader customerReader, Reader invoiceReader, 
			Parser.Format format) {

		readData(customerReader, invoiceReader, 
				ParserFactory.createParser(format));
	}

	/**
	 * Helper to read the customer and invoice data.
	 */
	private void readData(Reader customerReader, Reader invoiceReader, 
			Parser parser) {
		try {
			customers = parser.parseCustomers(customerReader)
					.collect(Collectors.toMap(Customer::getName, identity()));
			invoices = parser.parseInvoices(invoiceReader, customers)
					.collect(Collectors.toList());
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Couldn't load from given filenames.", ex);
		}
		
	}
	
	/**
	 * Get a collection of all customers.
	 */
	public Collection<Customer> getCustomers() {
		return customers.values();
	}
	
	/**
	 * Get a collection of all invoices.
	 */
	public Collection<Invoice> getInvoices() {
		return invoices;
	}
	
	/**
	 * Builds an {@link Outline2} representation of the invoices for the given
	 * customer, and generates the report. 
	 */
	public SortedSet<Invoice> getInvoicesForCustomer(String customerName) {

		Customer customer = customers.get(customerName);
		return (SortedSet<Invoice>) invoices.stream()
				.filter(inv -> inv.getCustomer().equals(customer))
				.collect(Collectors.toCollection(() -> new TreeSet<>
					((Reporter::compareByNumber))));
	}

	/**
	 * Builds an {@link Outline3} representation of invoices grouped by
	 * customer, and generates the report. 
	 */
	/*START String filename */
	public SortedMap<Customer,SortedSet<Invoice>> getInvoicesByCustomer() {
		
		return customers.values().stream()
			.collect(Collectors.toMap(identity(),
				c -> getInvoicesForCustomer(c.getName()), 
				(a, b) -> a, () -> new TreeMap<>(Reporter::compareByName)));
	}

	/**
	 * Builds an {@link Outline2} representation of overdue invoices, 
	 * and generates the report. 
	 */
	/*START String filename */
	public SortedSet<Invoice> getOverdueInvoices(LocalDate asOf) {
		
		return (SortedSet<Invoice>) invoices.stream()
			.filter(invoice -> invoice.isOverdue(asOf))
			.collect(Collectors.toCollection(() -> new TreeSet<>
				(Reporter::compareByDate)));
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CustomerWithVolume 
			implements Comparable<CustomerWithVolume> {

		private String customerName;
		private double volume;
		
		/**
		 * Compare "them" to "us" by volume, so as to get descending order.
		 */
		public int compareTo(CustomerWithVolume other) {
			return Double.compare(other.getVolume(), volume);
		}
	}
	
	public double getVolume(Customer customer) {
		return invoices.stream()
				.filter(inv -> inv.getCustomer().equals(customer))
				.mapToDouble(Invoice::getAmount)
				.sum();
	}
	
	public SortedSet<CustomerWithVolume> getCustomersByVolume() {
		return (TreeSet<CustomerWithVolume>) customers.values().stream()
				.map(c -> new CustomerWithVolume(c.getName(), getVolume(c)))
				.collect(Collectors.toCollection(TreeSet::new));
	}
}
