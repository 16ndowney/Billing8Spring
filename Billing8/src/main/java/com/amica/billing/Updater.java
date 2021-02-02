package com.amica.billing;

import static java.util.function.Function.identity;

import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.amica.acm.configuration.component.ComponentConfigurationsManager;
import com.amica.billing.parse.Producer;
import com.amica.billing.parse.Parser.Format;
import com.amica.escm.configuration.api.Configuration;

import lombok.extern.java.Log;

/**
 * Component that carries out specific updates to the AR data.
 * 
 * @author Will Provost
 */
@Log
public class Updater {

	public static final String CONFIGURATION_NAME = "Billing";
	public static final String CUSTOMER_FILE_PROPERTY =
			Reporter.class.getPackage().getName() + ".customerFile";
	public static final String INVOICE_FILE_PROPERTY =
			Reporter.class.getPackage().getName() + ".invoiceFile";

	private String customersFilename;
	private String invoicesFilename;
	private Producer parser;
	
	private Map<String,Customer> customers;
	private Map<Integer,Invoice> invoices;
	private int nextInvoiceNumber;

	/**
	 * Customer and invoice data is found in files whose names are provided
	 * using the configuration manager.
	 */
	public Updater(Configuration configuration) {
		
		customersFilename = configuration.getString(CUSTOMER_FILE_PROPERTY);
		invoicesFilename = configuration.getString(INVOICE_FILE_PROPERTY);
		parser = ParserFactory.createParser(configuration, Format.DEFAULT);
		load();
	}
	
	/**
	 * Customer and invoice data is found in files whose names are provided
	 * using the configuration manager.
	 */
	public Updater() {
		this(ComponentConfigurationsManager.getDefaultComponentConfiguration()
				.getConfiguration(CONFIGURATION_NAME));
	}

	/**
	 * Customer and invoice data is found in files of the given names.
	 */	
	public Updater(String customersFilename, String invoicesFilename,
			Format format) {
		this.customersFilename = customersFilename;
		this.invoicesFilename = invoicesFilename;
		this.parser = ParserFactory.createParser(format);
		load();
	}
	
	/**
	 * Customer and invoice data is found in files of the given names.
	 */	
	public Updater(String customersFilename, String invoicesFilename) {
		this(customersFilename, invoicesFilename, Format.DEFAULT);
	}
	
	/**
	 * Load data from files using the configured parser.
	 */
	public void load() {
		try (
			FileReader customerReader = new FileReader(customersFilename);
			FileReader invoiceReader = new FileReader(invoicesFilename);
		) {
			customers = parser.parseCustomers(new FileReader(customersFilename))
					.collect(Collectors.toMap(Customer::getName, identity()));
			invoices = parser.parseInvoices
				(new FileReader(invoicesFilename), customers)
					.collect(Collectors.toMap(Invoice::getNumber, identity(), 
							(x,y) -> x, TreeMap::new));
			nextInvoiceNumber = invoices.keySet().stream()
					.mapToInt(Integer::intValue).max().orElse(0) + 1;
		} catch (Exception ex) {
			log.log(Level.SEVERE, String.format("%s=%s", 
					CUSTOMER_FILE_PROPERTY, customersFilename));
			log.log(Level.SEVERE, String.format("%s=%s", 
					INVOICE_FILE_PROPERTY, invoicesFilename));
			log.log(Level.SEVERE, "Couldn't load files as configured", ex);
		}
	}
	
	/**
	 * Save data to files using the configured producer.
	 */
	public void save() {
		
		try ( 
			FileWriter customerWriter = new FileWriter(customersFilename);
			FileWriter invoiceWriter = new FileWriter(invoicesFilename);
		) {
			parser.produceCustomers(customers.values().stream(), 
					customerWriter);
			parser.produceInvoices(invoices.values().stream(), 
					invoiceWriter);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Couldn't save data.", ex);
		}
	}

	/**
	 * Create a customer with the given data and add it to the set.
	 */
	public void createCustomer(String firstName, String lastName, Terms terms) {
		Customer customer = new Customer(firstName, lastName, terms);
		if (!customers.containsKey(customer.getName())) {
			customers.put(customer.getName(), customer);
		} else {
			throw new IllegalArgumentException
				("There is already a customer with the name " + 
					customer.getName());
		}
	}

	/**
	 * Create an invoice with the given data and add it to the set.
	 * Invoice number is generated; invoice date is assumed to be today.
	 */
	public void createInvoice(String customerName, double amount) {
		
		if (customers.containsKey(customerName)) {
			Invoice invoice = new Invoice(nextInvoiceNumber++, 
					customers.get(customerName), amount, LocalDate.now(), null);
			invoices.put(invoice.getNumber(), invoice);
		} else {
			throw new IllegalArgumentException("No such customer: " + customerName);
		}
	}

	/**
	 * Set today's date as the paid date for the invoice with the given number.
	 */
	public void payInvoice(int invoiceNumber) {
		
		if (invoices.containsKey(invoiceNumber)) {
			Invoice invoice = invoices.get(invoiceNumber);
			if (invoice.getPaidDate() == null) {
				invoice.setPaidDate(LocalDate.now());
			} else {
				throw new IllegalStateException("Invoice " + invoiceNumber + 
						" has already been paid.");
			}
		} else {
			throw new IllegalArgumentException("No such invoice: " + invoiceNumber);
		}
	}
}
