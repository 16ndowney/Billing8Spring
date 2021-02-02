package com.amica.billing.parse;

import java.io.Reader;
import java.util.Map;
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
public interface Parser {
	
	enum Format { CSV, FLAT, EXPORT, EXCEL, JSON, DEFAULT }

	/**
	 * Returns a stream of {@link Customer}s, one for each text representation. 
	 */
	public Stream<Customer> parseCustomers(Reader customerReader);

	/**
	 * Returns a stream of {@link Invoice}s, one for each text representation.
	 * The provided map of customer names to customer records is used to 
	 * translate the customer names found in the text representation to
	 * references to {@link Customer} objects, which we're assuming 
	 * are loaded first. 
	 */
	public Stream<Invoice> parseInvoices(Reader invoiceReader, 
			Map<String, Customer> customers);
}
