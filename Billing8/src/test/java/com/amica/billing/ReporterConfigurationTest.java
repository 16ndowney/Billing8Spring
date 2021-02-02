package com.amica.billing;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.time.LocalDate;
import java.util.SortedMap;
import java.util.SortedSet;

import org.junit.BeforeClass;
import org.junit.Test;

import com.amica.billing.Reporter.CustomerWithVolume;
public class ReporterConfigurationTest {

	public static final String INPUT_FOLDER = "src/test/resources/data";
	@BeforeClass
	public static void setUpClass() {
		System.setProperty("server.env", "ReporterConfigurationTest");
	}
	
	private void testWithMainDataSet() throws IOException {
		
		Reporter reporter = new Reporter();
		
		final String CUSTOMER_NAME = "Janis Joplin";

		assertThat(reporter.getInvoicesForCustomer(CUSTOMER_NAME),
				hasSize(3));
		
		SortedMap<Customer,SortedSet<Invoice>> invoicesByCustomer =
				reporter.getInvoicesByCustomer();
		assertThat(invoicesByCustomer.keySet(), hasSize(13));
		Customer firstCustomer = invoicesByCustomer.firstKey(); 
		assertThat(firstCustomer.getName(), equalTo("Chet Atkins"));
		assertThat(invoicesByCustomer.get(firstCustomer)
				.first().getNumber(), equalTo(104));
		Customer lastCustomer = invoicesByCustomer.lastKey();
		assertThat(lastCustomer.getName(), equalTo("Lucinda Williams"));
		assertThat(invoicesByCustomer.get(lastCustomer)
				.last().getNumber(),equalTo(112));
		
		SortedSet<Invoice> overdue = reporter.getOverdueInvoices
				(LocalDate.of(2020, 12, 1));
		assertThat(overdue.first().getNumber(), equalTo(102));
		assertThat(overdue.last().getNumber(), equalTo(124));
		
		SortedSet<CustomerWithVolume> customersByVolume =
				reporter.getCustomersByVolume();
		assertThat(customersByVolume.first().getCustomerName(), 
				equalTo("Jerry Reed"));
		assertThat(customersByVolume.last().getCustomerName(), 
				equalTo("Janis Joplin"));
	}


	@Test
	public void testFromConfiguredFiles() throws IOException {
		testWithMainDataSet();
	}
}
