package com.amica.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.amica.billing.parse.Parser;
import com.amica.billing.parse.Producer;
import com.amica.billing.parse.Parser.Format;

@Configuration
@ComponentScan
public class InvoicePayment {
	
	private Producer parser;
	private static Updater updater;
	private ParserFactory factory = new ParserFactory();
	static ApplicationContext applicationContext;

	public static void main(String[] args) {
		System.setProperty("server.env", "developerworkstation");
		applicationContext = SpringApplication.run(InvoicePayment.class, args);
		updater =applicationContext.getBean(Updater.class);
		Producer producer = applicationContext.getBean(Producer.class);
		System.out.println(updater.getClass().getName());
	}
	
	@Bean
	public Producer producerBean() {
			parser = factory.createParser(Format.DEFAULT);
			
			return parser;
	}
	
	
}
