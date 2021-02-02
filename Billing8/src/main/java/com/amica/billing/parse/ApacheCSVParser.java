package com.amica.billing.parse;

import java.io.Reader;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;
import com.amica.billing.Terms;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

/**
 * A parser that can read a CSV format with certain expected columns.
 * 
 * @author Will Provost
 */
@Log
public class ApacheCSVParser implements Producer {

	private static final int CUSTOMER_COLUMNS = 3;
	private static final int CUSTOMER_FIRST_NAME_COLUMN = 0;
	private static final int CUSTOMER_LAST_NAME_COLUMN = 1;
	private static final int CUSTOMER_TERMS_COLUMN = 2;

	private static final int INVOICE_MIN_COLUMNS = 6;
	private static final int INVOICE_NUMBER_COLUMN = 0;
	private static final int INVOICE_FIRST_NAME_COLUMN = 1;
	private static final int INVOICE_LAST_NAME_COLUMN = 2;
	private static final int INVOICE_AMOUNT_COLUMN = 3;
	private static final int INVOICE_DATE_COLUMN = 4;
	private static final int INVOICE_PAID_DATE_COLUMN = 5;

	private CSVFormat format;
	private boolean writeHeaders;
	
	public ApacheCSVParser(boolean writeHeaders) {
		this(CSVFormat.DEFAULT, writeHeaders);
	}
	
	public ApacheCSVParser(CSVFormat format, boolean writeHeaders) {
		this.format = format;
		this.writeHeaders = writeHeaders;
	}
	
	public static ApacheCSVParser createExportParser() {
		return new ApacheCSVParser(CSVFormat.DEFAULT
				.withQuoteMode(QuoteMode.NON_NUMERIC)
				.withNullString("NULL")
				.withRecordSeparator("\n")
				.withFirstRecordAsHeader(), true);
	}
	
	public static ApacheCSVParser createExcelParser() {
		return new ApacheCSVParser(CSVFormat.EXCEL
				.withNullString("")
				.withRecordSeparator("\n"), false);
	}
	
	public CSVFormat getCSVFormat() {
		return format;
	}
	
	/**
	 * Helper that can parse one line of comma-separated text in order to
	 * produce a {@link Customer} object.
	 */
	private Customer parseCustomer(CSVRecord record) {
		if (record.size() == CUSTOMER_COLUMNS) {
			try {
				return new Customer(record.get(CUSTOMER_FIRST_NAME_COLUMN), 
						record.get(CUSTOMER_LAST_NAME_COLUMN), 
						Terms.valueOf(record.get(CUSTOMER_TERMS_COLUMN)));
			} catch (Exception ex) {
				log.warning(() -> 
					"Couldn't parse terms value, skipping customer: "+ record);
			}
		} else {
			log.warning(() -> 
				"Incorrect number of fields, skipping customer: " + record);
		}

		return null;
	}

	/**
	 * Helper that can parse one line of comma-separated text in order to
	 * produce an {@link Invoice} object.
	 */
	private Invoice parseInvoice(CSVRecord record, Map<String, Customer> customers) {
		DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		if (record.size() >= INVOICE_MIN_COLUMNS) {
			try {
				int number = Integer.parseInt(record.get(INVOICE_NUMBER_COLUMN));
				String first = record.get(INVOICE_FIRST_NAME_COLUMN);
				String last = record.get(INVOICE_LAST_NAME_COLUMN);
				double amount = Double.parseDouble
						(record.get(INVOICE_AMOUNT_COLUMN));
				
				LocalDate date = LocalDate.parse(record.get(INVOICE_DATE_COLUMN), parser);
				LocalDate paidDate = record.get(INVOICE_PAID_DATE_COLUMN) != null
						? LocalDate.parse(record.get(INVOICE_PAID_DATE_COLUMN), parser) 
						: null;

				Customer customer = customers.get(first + " " + last);
				if (customer != null) {
					return new Invoice(number, customer, amount, date, paidDate);
				} else {
					log.warning(() -> 
						"Unknown customer, skipping invoice: " + record);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				log.warning(() -> 
					"Couldn't parse values, skipping invoice: " + record);
			}
		} else {
			log.warning(() -> 
				"Incorrect number of fields, skipping invoice: " + record);
		}

		return null;
	}

	/**
	 * Helper to convert a customer to an array of fields.
	 */
	public Object[] produceCustomer(Customer customer) {
		return new Object[] {
			customer.getFirstName(),
			customer.getLastName(),
			customer.getTerms().toString()
		};
	}
	
	/**
	 * Helper to convert an invoice to an array of fields.
	 */
	private Object[] produceInvoice(Invoice invoice) {
		return new Object[] {
			invoice.getNumber(),
			invoice.getCustomer().getFirstName(),
			invoice.getCustomer().getLastName(),
			invoice.getAmount(),
			invoice.getTheDate(),
			invoice.getPaidDate()
		};
	}

	@SneakyThrows
	private void sneakyPrint(CSVPrinter printer, Object[] values) {
		printer.printRecord(values);
	}
	
	/**
	 * Consumes the given string streams and translates to {@link Customer}
	 * objects.
	 */
	public Stream<Customer> parseCustomers(Reader customerReader) {
		try {
			return StreamSupport.stream
				(format.parse(customerReader).spliterator(), false)
					.map(this::parseCustomer);
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
			return StreamSupport.stream
				(format.parse(invoiceReader).spliterator(), false)
					.map(record -> parseInvoice(record, customers));
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Couldn't parse customers file.", ex);
		}
		
		return Stream.empty();
	}

	/**
	 * Write the given stream to the given writer, producing a header
	 * if we're configured to do so, and otherwise using the
	 * configured format. 
	 */
	@SneakyThrows
	public void produceCustomers(Stream<Customer> customers, Writer writer) {
		if (writeHeaders) {
			writer.write("\"First\",\"Last\",\"Terms\"\n");
		}
		CSVPrinter printer = format.print(writer);
		customers.map(this::produceCustomer)
				.forEach(values -> sneakyPrint(printer, values));
	}

	/**
	 * Write the given stream to the given writer, producing a header
	 * if we're configured to do so, and otherwise using the
	 * configured format. 
	 */
	@SneakyThrows
	public void produceInvoices(Stream<Invoice> invoices, Writer writer) {
		if (writeHeaders) {
			writer.write("\"Number\",\"CustomerFirst\",\"CustomerLast\",\"Amount\",\"Date\",\"Paid\"\n");
		}
		CSVPrinter printer = format.print(writer);
		invoices.map(this::produceInvoice)
				.forEach(values -> sneakyPrint(printer, values));
		
	}
}
