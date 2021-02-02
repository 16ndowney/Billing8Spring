package com.amica.billing;

/**
 * Stateful enumerated type representing possible payment terms.
 * Each value holds the number of days allowed for payment,
 * and supports easy conversion between the enumerated value and
 * the primitive integer type.
 *
 * @author Will Provost
 */
public enum Terms {
    CASH(0),
    CREDIT_30(30),
    CREDIT_45(45),
    CREDIT_60(60),
    CREDIT_90(90);

    private Terms(int days) {
        this.days = days;
    }

    private int days;

    public int getDays() {
        return days;
    }

    public static Terms fromDays(int days) {
        for (Terms terms : values()) {
            if (terms.getDays() == days) {
                return terms;
            }
        }

        return null;
    }
}
