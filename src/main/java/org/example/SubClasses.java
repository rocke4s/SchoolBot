package org.example;

public enum SubClasses {
    CLAS_A("А"),
    CLAS_B("Б"),
    CLAS_C("В"),
    CLAS_D("Г"),
    CLAS_F("Д");

    private String subClasses;

    SubClasses(String subClasses) {
        this.subClasses = subClasses;
    }

    public static boolean searchSubClasses(String numberSubClass) {
        for (SubClasses val : values()) {
            if (val.subClasses.equals(numberSubClass)) {
                return true;
            }
        }
        return false;
    }
}
