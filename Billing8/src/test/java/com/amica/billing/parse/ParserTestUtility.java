package com.amica.billing.parse;

import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amica.billing.Customer;
import com.amica.billing.Invoice;
import com.amica.billing.Terms;

public class ParserTestUtility {

	public static final Customer[] GOOD_CUSTOMERS = {
		new Customer("Customer", "One", Terms.CASH),
		new Customer("Customer", "Two", Terms.CREDIT_45),
		new Customer("Customer", "Three", Terms.CREDIT_30)
	};
	
	public static final Map<String,Customer> GOOD_CUSTOMERS_MAP =
		Stream.of(GOOD_CUSTOMERS).collect(Collectors.toConcurrentMap
			(Customer::getName, Function.identity()));

	public static final Customer[] BAD_CUSTOMERS = {
		GOOD_CUSTOMERS[2]
	};

	public static final Invoice[] GOOD_INVOICES = {
		new Invoice(1, GOOD_CUSTOMERS[0], 100, LocalDate.of(2021,  1,  4), null),
		new Invoice(2, GOOD_CUSTOMERS[1], 200, LocalDate.of(2021,  1,  4), LocalDate.of(2021, 1, 5)),
		new Invoice(3, GOOD_CUSTOMERS[1], 300, LocalDate.of(2021,  1,  6), null),
		new Invoice(4, GOOD_CUSTOMERS[1], 400, LocalDate.of(2020, 11, 11), null),
		new Invoice(5, GOOD_CUSTOMERS[2], 500, LocalDate.of(2021,  1,  4), LocalDate.of(2021, 1, 8)),
		new Invoice(6, GOOD_CUSTOMERS[2], 600, LocalDate.of(2020, 12,  4), null)
	};

	public static final Invoice[] BAD_INVOICES = {
		GOOD_INVOICES[0],
		GOOD_INVOICES[1],
		GOOD_INVOICES[5]
	};
}
