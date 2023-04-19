package org.example;

public enum DayOfWeek {
    UNKNOWN("?", -1),
    SUNDAY("Понедельник", 1),
    MONDAY("Вторник", 2),
    TUESDAY("Среда", 3),
    WEDNESDAY("Четверг", 4),
    THURSDAY("Пятница", 5),
    FRIDAY("Суббота", 6),
    SATURDAY("воскресенье", 7); // на всякий случай пусть будет
    private String dayOfWeek;
    private int numberOfWeek;

    DayOfWeek(String dayOfWeek, int numberOfWeek) {
        this.dayOfWeek = dayOfWeek;
        this.numberOfWeek = numberOfWeek;
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
}
