package com.kingdee.csce.shr.utils;

import java.time.LocalDate;

public class CscecLocalDateUtil {
    public static LocalDate getFirstDayOfCurrentYear() {
        return getFirstDayOfYear(LocalDate.now());
    }
    public static LocalDate getFirstDayOfYear(LocalDate date) {
        return LocalDate.of(date.getYear(), 1, 1);
    }
    public static LocalDate getLastDayOfCurrentYear() {
        return getLastDayOfYear(LocalDate.now());
    }
    public static LocalDate getLastDayOfYear(LocalDate date) {
        return LocalDate.of(date.getYear(), 12, 31);
    }
}
