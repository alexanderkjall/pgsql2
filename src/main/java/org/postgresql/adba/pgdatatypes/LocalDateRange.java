package org.postgresql.adba.pgdatatypes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class LocalDateRange implements Comparable<LocalDateRange> {
  private boolean empty;
  private LocalDate lower;
  private LocalDate upper;
  private boolean lowerInclusive;
  private boolean upperInclusive;

  /**
   * creates an empty range.
   */
  public LocalDateRange() {
    empty = true;
  }

  /**
   * Creates an range, if the boundaries are next to each other, then the empty flag will be set.
   *
   * @param lower lower bound
   * @param upper upper bound
   * @param lowerInclusive if lower is inclusive, '[' in postgresql syntax
   * @param upperInclusive if upper is inclusive, ']' in postgresql syntax
   */
  public LocalDateRange(LocalDate lower, LocalDate upper, boolean lowerInclusive, boolean upperInclusive) {
    this.lower = lower;
    this.upper = upper;
    this.lowerInclusive = lowerInclusive;
    this.upperInclusive = upperInclusive;

    canonicalize();
  }

  private void canonicalize() {
    if (!lowerInclusive) {
      lowerInclusive = true;
      if (lower != null) {
        lower = lower.minusDays(1);
      }
    }
    if (upperInclusive) {
      upperInclusive = false;
      if (upper != null) {
        upper = upper.plusDays(1);
      }
    }

    if (lower != null && upper != null && lower.isAfter(upper)) {
      throw new IllegalArgumentException("range lower bound must be less than or equal to range upper bound");
    }

    empty = lower != null && upper != null && lower.equals(upper.minusDays(1));
  }

  public boolean isEmpty() {
    return empty;
  }

  public LocalDate getLower() {
    return lower;
  }

  /**
   * sets the lower bound, and then runs the canonicalization function.
   *
   * @param lower sets the lower bound, must be lower or equal to the upper bound
   */
  public void setLower(LocalDate lower) {
    this.lower = lower;

    canonicalize();
  }

  public LocalDate getUpper() {
    return upper;
  }

  /**
   * sets the upper bound, and then runs the canonicalization function.
   *
   * @param upper sets the lower bound, must be higher or equal to the lower bound
   */
  public void setUpper(LocalDate upper) {
    this.upper = upper;

    canonicalize();
  }

  public boolean isLowerInclusive() {
    return lowerInclusive;
  }

  /**
   * sets whether the lower bound is inclusive or not, and then runs the canonicalization function.
   *
   * @param lowerInclusive sets the lower inclusive bound flag
   */
  public void setLowerInclusive(boolean lowerInclusive) {
    this.lowerInclusive = lowerInclusive;

    canonicalize();
  }

  public boolean isUpperInclusive() {
    return upperInclusive;
  }

  /**
   * sets whether the upper bound is inclusive or not, and then runs the canonicalization function.
   *
   * @param upperInclusive sets the upper inclusive bound flag
   */
  public void setUpperInclusive(boolean upperInclusive) {
    this.upperInclusive = upperInclusive;

    canonicalize();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocalDateRange numericRange = (LocalDateRange) o;
    if (empty && numericRange.empty) {
      return true;
    }
    return lowerInclusive == numericRange.lowerInclusive
        && upperInclusive == numericRange.upperInclusive
        && Objects.equals(lower, numericRange.lower)
        && Objects.equals(upper, numericRange.upper);
  }

  @Override
  public int hashCode() {
    return Objects.hash(empty, lower, upper, lowerInclusive, upperInclusive);
  }

  @Override
  public int compareTo(LocalDateRange lr) {
    if (empty && lr.empty) {
      return 0;
    }

    int c = Objects.compare(lr.lower, lower, LocalDate::compareTo);

    if (c != 0) {
      return c;
    }

    c = Objects.compare(lr.upper, upper, LocalDate::compareTo);

    if (c != 0) {
      return c;
    }

    c = Boolean.compare(lr.lowerInclusive, lowerInclusive);

    if (c != 0) {
      return c;
    }

    return Boolean.compare(lr.upperInclusive, upperInclusive);
  }

  @Override
  public String toString() {
    if (empty) {
      return "empty";
    }

    return (lowerInclusive ? "[" : "(")
        + (lower == null ? "" : lower.format(DateTimeFormatter.ISO_LOCAL_DATE)) + ","
        + (upper == null ? "" : upper.format(DateTimeFormatter.ISO_LOCAL_DATE))
        + (upperInclusive ? "]" : ")");
  }
}
