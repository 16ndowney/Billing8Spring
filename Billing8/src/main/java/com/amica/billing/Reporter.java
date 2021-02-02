package com.amica.billing;

import static java.util.function.Function.identity;

import java.io.FileNotFoundException;
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
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amica.acm.configuration.component.ComponentConfigurationsManager;
import com.amica.billing.parse.Parser;
import com.amica.billing.parse.Producer;
import com.amica.escm.configuration.api.Configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.java.Log;

/**
 * This is the central component of the system. It reads a file of
 * {@link Customer}s and a file of {@link Invoice}s, using configurable
 * {@link Parser}s so as to to handle different file formats; and then can
 * produce reports based on a few different queries and relying on a generic
 * {@link TextReporter report generator}.
 * 
 * @author Will Provost
 */
@Log
@Component
public class Reporter {

	public static final String CONFIGURATION_NAME = "Billing";
	public static final String CUSTOMER_FILE_PROPERTY = Reporter.class.getPackage().getName() + ".customerFile";
	public static final String INVOICE_FILE_PROPERTY = Reporter.class.getPackage().getName() + ".invoiceFile";

	@Autowired
	@Getter
	@Setter
	private Producer parser;

	private Supplier<Reader> customerReaderFactory;
	private Supplier<Reader> invoiceReaderFactory;

	public static int compareByName(Customer a, Customer b) {
		return (a.getLastName() + a.getFirstName()).compareTo(b.getLastName() + b.getFirstName());
	}

	public static int compareByNumber(Invoice a, Invoice b) {
		return Integer.compare(a.getNumber(), b.getNumber());
	}

	public static int compareByDate(Invoice a, Invoice b) {
		return a.getTheDate().compareTo(b.getTheDate());
	}

	private Map<String, Customer> customers;
	private List<Invoice> invoices;

	/**
	 * Customer and invoice data is found in files whose names are provided using
	 * the configuration manager.
	 */
	public Reporter(Configuration configuration) {

		String customerFile = configuration.getString(CUSTOMER_FILE_PROPERTY);
		String invoiceFile = configuration.getString(INVOICE_FILE_PROPERTY);

		customerReaderFactory = () -> {
			try {
				return new FileReader(customerFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		};
		invoiceReaderFactory = () -> {
			try {
				return new FileReader(invoiceFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		};

	}

	/**
	 * Customer and invoice data is found in files whose names are provided using
	 * the configuration manager.
	 */
	public Reporter() {
		this(ComponentConfigurationsManager.getDefaultComponentConfiguration().getConfiguration(CONFIGURATION_NAME));
	}

	/**
	 * Provide readers with customer and invoice data, and the data format. The
	 * invoice data is expected to include customer names, and in loading the data
	 * we re-connect the invoices so that they refer directly to the customer
	 * objects in memory.
	 */
//	public Reporter(Reader customerReader, Reader invoiceReader) {
//		this(customerReader, invoiceReader);
//	}

	/**
	 * Provide the locations of a file of customer data and a file of invoice data.
	 * The invoice data is expected to include customer names, and in loading the
	 * data we re-connect the invoices so that they refer directly to the customer
	 * objects in memory.
	 */
	public Reporter(Reader customerReader, Reader invoiceReader) {
		customerReaderFactory = () -> customerReader;
		invoiceReaderFactory = () -> invoiceReader;
		

	}

	/**
	 * Helper to read the customer and invoice data.
	 */
	@PostConstruct
	public void readData() {
		try (Reader customerReader = customerReaderFactory.get(); Reader invoiceReader = invoiceReaderFactory.get();) {
			customers = parser.parseCustomers(customerReader).collect(Collectors.toMap(Customer::getName, identity()));
			invoices = parser.parseInvoices(invoiceReader, customers).collect(Collectors.toList());
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
		return (SortedSet<Invoice>) invoices.stream().filter(inv -> inv.getCustomer().equals(customer))
				.collect(Collectors.toCollection(() -> new TreeSet<>((Reporter::compareByNumber))));
	}

	/**
	 * Builds an {@link Outline3} representation of invoices grouped by customer,
	 * and generates the report.
	 */
	/* START String filename */
	public SortedMap<Customer, SortedSet<Invoice>> getInvoicesByCustomer() {

		return customers.values().stream().collect(Collectors.toMap(identity(),
				c -> getInvoicesForCustomer(c.getName()), (a, b) -> a, () -> new TreeMap<>(Reporter::compareByName)));
	}

	/**
	 * Builds an {@link Outline2} representation of overdue invoices, and generates
	 * the report.
	 */
	/* START String filename */
	public SortedSet<Invoice> getOverdueInvoices(LocalDate asOf) {

		return (SortedSet<Invoice>) invoices.stream().filter(invoice -> invoice.isOverdue(asOf))
				.collect(Collectors.toCollection(() -> new TreeSet<>(Reporter::compareByDate)));
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CustomerWithVolume implements Comparable<CustomerWithVolume> {

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
		return invoices.stream().filter(inv -> inv.getCustomer().equals(customer)).mapToDouble(Invoice::getAmount)
				.sum();
	}

	public SortedSet<CustomerWithVolume> getCustomersByVolume() {
		return (TreeSet<CustomerWithVolume>) customers.values().stream()
				.map(c -> new CustomerWithVolume(c.getName(), getVolume(c)))
				.collect(Collectors.toCollection(TreeSet::new));
	}
	
	
}
