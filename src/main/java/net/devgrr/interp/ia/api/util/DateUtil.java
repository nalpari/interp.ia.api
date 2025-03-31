package net.devgrr.interp.ia.api.util;

import java.time.LocalDate;

public class DateUtil {
  public static boolean isValidDateRange(LocalDate startDate, LocalDate endDate) {
    return startDate != null
        && endDate != null
        && !startDate.equals(LocalDate.MIN)
        && !endDate.equals(LocalDate.MIN)
        && !startDate.isAfter(endDate);
  }
}
