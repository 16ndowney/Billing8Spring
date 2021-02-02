package com.amica.billing;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Simple JavaBean representing a customer.
 *
 * @author Will Provost
 */
@Data
@EqualsAndHashCode(of={"firstName", "lastName"})
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    private String firstName;
    private String lastName;
    private Terms terms;
    
    @JsonIgnore
    public String getName() {
    	return firstName + " " + lastName;
    }
    
    @Override
    public String toString() {
    	return "Customer: " + getName();
    }
}
