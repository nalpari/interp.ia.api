package net.devgrr.interp.ia.api.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
  public static boolean isValidDateRange(LocalDate startDate, LocalDate endDate) {
    return startDate != null
        && endDate != null
        && !startDate.equals(LocalDate.MIN)
        && !endDate.equals(LocalDate.MIN)
        && !startDate.isAfter(endDate);
  }

  public static String formatDate(Object date) {
    if (date instanceof LocalDate) {
      return ((LocalDate) date).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    } else if (date instanceof LocalDateTime) {
      return ((LocalDateTime) date).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    return "";
  }

  public static String formatDateTimeNow(String pattern) {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
  }
}
