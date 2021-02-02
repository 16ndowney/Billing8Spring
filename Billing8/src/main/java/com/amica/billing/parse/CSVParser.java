package com.amica.billing.parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Stream;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;
import com.amica.billing.Terms;

import lombok.extern.java.Log;

/**
 * A parser that can read a CSV format with certain expected columns.
 * 
 * @author Will Provost
 */
@Log
public class CSVParser implements Producer {

	private static final int CUSTOMER_COLUMNS = 3;
	private static final int CUSTOMER_FIRST_NAME_COLUMN = 0;
	private static final int CUSTOMER_LAST_NAME_COLUMN = 1;
	private static final int CUSTOMER_TERMS_COLUMN = 2;

	private static final int INVOICE_MIN_COLUMNS = 5;
	private static final int INVOICE_NUMBER_COLUMN = 0;
	private static final int INVOICE_FIRST_NAME_COLUMN = 1;
	private static final int INVOICE_LAST_NAME_COLUMN = 2;
	private static final int INVOICE_AMOUNT_COLUMN = 3;
	private static final int INVOICE_DATE_COLUMN = 4;
	private static final int INVOICE_PAID_DATE_COLUMN = 5;

	/**
	 * Helper that can parse one line of comma-separated text in order to
	 * produce a {@link Customer} object.
	 */
	private Customer parseCustomer(String line) {
		String[] fields = line.split(",");
		if (fields.length == CUSTOMER_COLUMNS) {
			try {
				String termsField = fields[CUSTOMER_TERMS_COLUMN];
				Terms terms = termsField.equals(Terms.CASH.toString()) 
						? Terms.CASH
						: Terms.fromDays(Integer.parseInt(termsField));
				return new Customer(fields[CUSTOMER_FIRST_NAME_COLUMN], 
						fields[CUSTOMER_LAST_NAME_COLUMN], terms);
			} catch (Exception ex) {
				log.warning(() -> 
					"Couldn't parse terms value, skipping customer: "+ line);
			}
		} else {
			log.warning(() -> 
				"Incorrect number of fields, skipping customer: " + line);
		}

		return null;
	}

	/**
	 * Helper that can parse one line of comma-separated text in order to
	 * produce an {@link Invoice} object.
	 */
	private Invoice parseInvoice(String line, Map<String, Customer> customers) {
		DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String[] fields = line.split(",");
		if (fields.length >= INVOICE_MIN_COLUMNS) {
			try {
				int number = Integer.parseInt(fields[INVOICE_NUMBER_COLUMN]);
				String first = fields[INVOICE_FIRST_NAME_COLUMN];
				String last = fields[INVOICE_LAST_NAME_COLUMN];
				double amount = Double.parseDouble
						(fields[INVOICE_AMOUNT_COLUMN]);
				
				LocalDate date = LocalDate.parse(fields[INVOICE_DATE_COLUMN], parser);
				LocalDate paidDate = fields.length > INVOICE_PAID_DATE_COLUMN 
						? LocalDate.parse(fields[INVOICE_PAID_DATE_COLUMN], parser) 
						: null;

				Customer customer = customers.get(first + " " + last);
				if (customer != null) {
					return new Invoice(number, customer, amount, date, paidDate);
				} else {
					log.warning(() -> 
						"Unknown customer, skipping invoice: " + line);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				log.warning(() -> 
					"Couldn't parse values, skipping invoice: " + line);
			}
		} else {
			log.warning(() -> 
				"Incorrect number of fields, skipping invoice: " + line);
		}

		return null;
	}

	/**
	 * Helper to write a CSV representation of one customer.
	 */
	public String formatCustomer(Customer customer) {
		return String.format("%s,%s,%s\n", 
				customer.getFirstName(), customer.getLastName(), 
				customer.getTerms().toString().replace("CREDIT_", ""));
	}
	
	/**
	 * Helper to write a CSV representation of one invoice.
	 */
	public String formatInvoice(Invoice invoice) {
		LocalDate paid = invoice.getPaidDate();
		return String.format("%d,%s,%s,%.2f,%s%s\n", invoice.getNumber(),
				invoice.getCustomer().getFirstName(), 
				invoice.getCustomer().getLastName(),
				invoice.getAmount(), invoice.getTheDate().toString(),
				paid != null ? ("," + paid.toString()) : "");
	}

	/**
	 * Consumes the given string streams and translates to {@link Customer}
	 * objects.
	 */
	public Stream<Customer> parseCustomers(Reader customerReader) {

		return new BufferedReader(customerReader).lines()
				.map(this::parseCustomer)
				.filter(customer -> customer != null);

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

		return new BufferedReader(invoiceReader).lines()
				.map(line -> parseInvoice(line, customers))
				.filter(invoice -> invoice != null);
	}

	/**
	 * Maps each customer to a string representation and writes it.
	 */
	public void produceCustomers(Stream<Customer> customers, Writer writer) {
		customers.map(this::formatCustomer).forEach(record -> {
			try {
				writer.write(record);
			} catch (IOException ex) {
				log.warning(() -> "Couldn't write " + record);
			}
		});
	}

	/**
	 * Maps each invoice to a string representation and writes it.
	 */
	public void produceInvoices(Stream<Invoice> invoices, Writer writer) {
		invoices.map(this::formatInvoice).forEach(record -> {
			try {
				writer.write(record);
			} catch (IOException ex) {
				log.warning(() -> "Couldn't write " + record);
			}
		});
		
	}
}
