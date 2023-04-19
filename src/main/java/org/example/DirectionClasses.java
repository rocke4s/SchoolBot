package org.example;

public enum DirectionClasses {

    HUMANITARIAN("гуманитарный профиль"),
    NATURE_SINCE("естественно-научный профиль"),
    TECHNOLOGICAL("технологческий профиль");

    private String directionClasses;

    DirectionClasses(String directionClasses) {
        this.directionClasses = directionClasses;
    }

    public static boolean searchDirectionClasses(String directionClasses) {
        for (DirectionClasses val : values()) {
            if (val.directionClasses.equals(directionClasses)) {
                return true;
            }
        }
        return false;
    }
}

