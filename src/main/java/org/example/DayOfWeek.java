package org.example;

public enum DayOfWeek {
    UNKNOWN("?", -1, "?"),
    SUNDAY("Понедельник", 1, ""),
    MONDAY("Вторник", 2, ""),
    TUESDAY("Среда", 3, "Среду"),
    WEDNESDAY("Четверг", 4, ""),
    THURSDAY("Пятница", 5, "Пятницу"),
    FRIDAY("Суббота", 6, "Субботу"),
    SATURDAY("Воскресенье", 0, ""); // на всякий случай пусть будет
    private String dayOfWeek;
    private int numberOfWeek;


    private String dayOfWeekEnd;

    DayOfWeek(String dayOfWeek, int numberOfWeek, String dayOfWeekEnd) {
        this.dayOfWeek = dayOfWeek;
        this.numberOfWeek = numberOfWeek;
        this.dayOfWeekEnd = dayOfWeekEnd;
    }

    public static boolean searchDayOfWeek(String dayOfWeek) {
        for (DayOfWeek val : values()) {
            if (val.dayOfWeek.equals(dayOfWeek)) {
                return true;
            }
        }
        return false;
    }

    public static String byDayOfWeek(int numberOfWeekOfWeek) {
        for (DayOfWeek val : values()) {
            if (val.numberOfWeek == numberOfWeekOfWeek) {
                return val.dayOfWeek;
            }
        }
        return UNKNOWN.dayOfWeek;
    }

    public static String byDayOfWeekEnds(int numberOfWeekOfWeek) {

        if (TUESDAY.numberOfWeek == numberOfWeekOfWeek) {
            return TUESDAY.dayOfWeekEnd;
        } else if (THURSDAY.numberOfWeek == numberOfWeekOfWeek) {
            return THURSDAY.dayOfWeekEnd;
        } else if (FRIDAY.numberOfWeek == numberOfWeekOfWeek) {
            return FRIDAY.dayOfWeekEnd;
        }
        return byDayOfWeek(numberOfWeekOfWeek);
    }
}
