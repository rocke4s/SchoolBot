package org.example;

/**
 * перечисление разрешенных пользователей на изменение расписания
 */
public enum AdminShedule {
    ONE(743234635),
    TWO(1188351220),
    THREE(5959939548L),
    FOURE(5471231917L);

    private long idUser;

    AdminShedule(long idUser) {
        this.idUser = idUser;
    }

    public static boolean searchAdmin(long idUser) {
        for (AdminShedule val : values()) {
            if (val.idUser == idUser) {
                return true;
            }
        }
        return false;
    }

}
