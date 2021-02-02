package com.amica.billing.parse;

import java.io.Writer;
import java.util.stream.Stream;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;

/**
 * Represents a component that can read text lines and translate them into
 * {@link Customer} and {@link Invoice} objects. The text format is not 
 * specified, but implementations may be dedicated to specific formats. 
 * 
 * @author Will Provost
 */
public interface Producer extends Parser {
	
	/**
	 * Writes the given stream of customers to the given writer. 
	 */
	public void produceCustomers(Stream<Customer> customers, Writer writer);

	/**
	 * Writes the given stream of invoices to the given writer.
	 */
	public void produceInvoices(Stream<Invoice> invoices, Writer writer);
}
