package org.example;

public enum Classes {
    CLAS_1("1"),
    CLAS_2("2"),
    CLAS_3("3"),
    CLAS_4("4"),
    CLAS_5("5"),
    CLAS_6("6"),
    CLAS_7("7"),
    CLAS_8("8"),
    CLAS_9("9"),
    CLAS_10("10"),
    CLAS_11("11");

    private String classes;

    Classes(String classes) {
        this.classes = classes;
    }

    public static boolean searchClass(String numberClass) {
        for (Classes val : values()) {
            if (val.classes.equals(numberClass)) {
                return true;
            }
        }
        return false;
    }
}
