package org.example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBConnect {
    String[] DOTW = new String[]{"ПОНЕДЕЛЬНИК", "ВТОРНИК", "СРЕДА",
            "ЧЕТВЕРГ", "ПЯТНИЦА", "СУББОТА"};
    Map<String, Integer> kk = new HashMap<>();
    int h = 1;
    List<String> newSchedules = new ArrayList<>();
    final ConfigBot configBot = new ConfigBot();

    public Connection connects() throws SQLException, IOException {//общее подключение
        Connection connection = DriverManager.getConnection("jdbc:sqlite:botSchedule.db");
        return connection;
    }

    public Integer getPage(Long chatId) throws SQLException, IOException {
        int School = 1;
        Statement statement = connects().createStatement();
        ResultSet rs = statement.executeQuery("select page from users where user_id=" + chatId);
        rs.next();
        School = rs.getInt("page");
        rs.close();
        statement.close();
        connects().close();
        return School;
    }

    public void setPage(Long chatId, int page) throws SQLException, IOException {
        Statement statement = connects().createStatement();
        statement.executeUpdate("UPDATE users set page = " + page + " where user_id=" + chatId);
        statement.close();
        connects().close();
    }

    public List<String> getAllSchool() throws SQLException, IOException {
        List<String> masSchool = new ArrayList<>();
        Statement statement = connects().createStatement();
        ResultSet rs = statement.executeQuery("select school from school");
        while (rs.next()) {
            masSchool.add(rs.getString("school"));
        }
        rs.close();
        statement.close();
        connects().close();
        return masSchool;
    }

    public List<String> getAllClass(String school) throws SQLException, IOException {
        List<String> masClass = new ArrayList<>();
        Statement statement = connects().createStatement();
        ResultSet rs = statement.executeQuery("SELECT distinct class" +
                " from schedule" +
                " where school='" + school + "' order by class");
        while (rs.next()) {
            masClass.add(rs.getString("class"));
        }
        rs.close();
        statement.close();
        connects().close();
        return masClass;
    }

    public String getSchool(Long chatId) throws Exception {
        String School = "";
        Statement statement = connects().createStatement();
        ResultSet rs = statement.executeQuery("select user_school from users where user_id=" + chatId);
        rs.next();
        School = rs.getString("user_school");
        rs.close();
        statement.close();
        connects().close();
        return School;
    }

    public String[] getClassProfile(Long chatId, String numberAndletterclass) throws SQLException, IOException {
        List<String> listClassProf = new ArrayList<>();
        Statement statement = connects().createStatement();
        ResultSet rs = statement.executeQuery("SELECT distinct  class FROM schedule where class like '" + numberAndletterclass + "%'");
        while (rs.next()) {
            listClassProf.add(rs.getString("class").substring(3));
        }
        String[] listProf = new String[listClassProf.size()];
        for (int x = 0; x < listClassProf.size(); x++) {
            listProf[x] = listClassProf.get(x);
        }
        rs.close();
        statement.close();
        connects().close();
        return listProf;
    }

//    public String[] getClassLetter(Long chatId, String numberClass) throws SQLException {
//        List<String> listClassLetter = new ArrayList<>();
//        Statement statement = connects().createStatement();
//        ResultSet rs = statement.executeQuery("SELECT distinct  class FROM schedule where class like '" + numberClass + "%'");
//        while (rs.next()) {
//            listClassLetter.add(rs.getString("class").substring(2, 3));
//        }
//        String[] ClassLetters = new String[listClassLetter.size()];
//        for (int x = 0; x < listClassLetter.size(); x++) {
//            ClassLetters[x] = listClassLetter.get(x);
//        }
//        rs.close();
//        statement.close();
//        connects().close();
//        return ClassLetters;
//    }

    public String getSubToSchedule(Long chatId) throws Exception {
        String Sub = "";

        Statement statement = connects().createStatement();
        ResultSet rs = statement.executeQuery("select subtoschedule from  users where user_id=" + chatId);
        if (!rs.next()) {
            statement.close();
            connects().close();
            rs.close();
            return null;
        } else {
            Sub = rs.getString("subtoschedule");
            rs.close();
            statement.close();
            connects().close();
        }
        return Sub;
    }

    public List<String> getAllUsers() throws Exception {
        List<String> allUser = new ArrayList<>();
        Statement statement = connects().createStatement();
        ResultSet rs = statement.executeQuery("select user_id from users");
        while (rs.next()) {
            allUser.add(rs.getString("user_id"));
        }
        rs.close();
        statement.close();
        connects().close();
        return allUser;
    }

    public List<String> getUserNewScheduleInSchool(String newScheduleSchool, String newScheduleClass) throws Exception {
        List<String> masSchool = new ArrayList<>();
        Statement statement = connects().createStatement();
        ResultSet rs = statement.executeQuery("select user_id from users where user_school='" + newScheduleSchool + "' and user_class='" + newScheduleClass + "'");
        int x = 0;
        while (rs.next()) {
            masSchool.add(rs.getString("user_id"));
            x++;
        }
        rs.close();
        statement.close();
        connects().close();
        return masSchool;
    }


    public void setUserData(Long chatId, String data, String typeData) throws Exception {
        Statement statement = connects().createStatement();
        statement.executeUpdate("UPDATE users set " + typeData + " = '" + data + "' where user_id=" + chatId);
        statement.close();
        connects().close();
    }


    //TODO есть мнение что 4 метода ниже идут в один, внезапно. просто еще три строки передаешь где нужные поля, таблицы, и поля для выборки
    public String getState(Long chatId) throws Exception {
        String state = "";
        Statement statement = connects().createStatement();
        ResultSet rs = statement.executeQuery("select user_state from users where user_id=" + chatId);
        rs.next();
        state = rs.getString("user_state");
        rs.close();
        statement.close();
        connects().close();
        return state;
    }

    public String getGlobalState(Long chatId) throws Exception {
        String state = "";
        Statement statement = connects().createStatement();
        ResultSet rs = statement.executeQuery("select global_state from users where user_id=" + chatId);
        rs.next();
        state = rs.getString("global_state");
        rs.close();
        statement.close();
        connects().close();
        return state;
    }

    public String getChatId(Long chatId) throws Exception {
        String user = "";

        Statement statement = connects().createStatement();
        ResultSet rs = statement.executeQuery("select user_id from users where user_id=" + chatId);
        if (!rs.next()) {
            statement.close();
            connects().close();
            rs.close();
            return null;
        } else {
            user = rs.getString("user_id");
            rs.close();
            statement.close();
            connects().close();
        }
        return user;
    }

    public String getLetter(Long chatId) throws Exception {
        String letter = "";

        Statement statement = connects().createStatement();
        ResultSet rs = statement.executeQuery("select user_selectwletter from users where user_id=" + chatId);
        if (rs.next()) {
            letter = rs.getString("user_selectwletter");
        } else {
            return null;
        }
        rs.close();
        statement.close();
        connects().close();
        return letter;
    }

    public String getLetterAndNumberClass(Long chatId) throws Exception {
        String letter = "";

        Statement statement = connects().createStatement();
        ResultSet rs = statement.executeQuery("select user_selectwletter, user_selectclass from users where user_id=" + chatId);
        if (rs.next()) {
            letter = rs.getString("user_selectclass");
            letter += rs.getString("user_selectwletter");
        } else {
            return null;
        }
        rs.close();
        statement.close();
        connects().close();
        return letter;
    }

    public String getClass(Long chatId) throws Exception {
        String clas = "";

        Statement statement = connects().createStatement();
        ResultSet rs = statement.executeQuery("select user_class from users where user_id=" + chatId);
        rs.next();
        clas = rs.getString("user_class");
        rs.close();
        statement.close();
        connects().close();
        return clas;
    }

    public String getSelectClass(Long chatId) throws Exception {
        String selectclas = "";

        Statement statement = connects().createStatement();
        ResultSet rs = statement.executeQuery("select user_selectclass, user_selectwletter from users where user_id=" + chatId);
        rs.next();
        selectclas = rs.getString("user_selectclass");
        rs.close();
        statement.close();
        connects().close();
        return selectclas;
    }

    public void createUser(Long chatId) throws Exception {

        Statement statement = connects().createStatement();
        statement.executeUpdate("INSERT INTO users (user_id, user_state, user_selectclass, user_selectwletter) values ("
                + chatId + ", 'create','','')");
        statement.close();
        connects().close();
    }

    public List<String> getNewScheduleFromSchool(String school) throws Exception {
        List<String> ScheduleNew = new ArrayList<>();
        Statement statement = connects().createStatement();
        ResultSet rs = statement.executeQuery("Select lesson_id,lesson,class,dotw, school from schedule_vrm except Select lesson_id,lesson,class,dotw, school from schedule where school='" + school + "'");
        while (rs.next()) {
            ScheduleNew.add("{" + rs.getString("class") + " в " + rs.getString("dotw") +
                    "} урок - (№" + rs.getString("lesson_id") + " : " + rs.getString("lesson") + ")");
        }
        rs.close();
        statement.close();
        connects().close();
        return ScheduleNew;
    }

    public void deleteAccount(Long chatId) throws Exception {

        Statement statement = connects().createStatement();
        statement.executeUpdate("DELETE FROM users where user_id='" + chatId + "'");
        statement.close();
        connects().close();
    }

    //    public yourClass changeMethodName(XSSFRow rowDOTW, XSSFRow rowLesson, XSSFSheet myExcelSheet, int m, String insertTableSQL,
//                                 Statement statement, XSSFRow rowClass, String school) {
//        yourClass.setRowDOTW(myExcelSheet.getRow(kk.get(DOTW[m])));
//        yourClass.setRowLesson(myExcelSheet.getRow(kk.get(DOTW[m])));
//        addOneDay(insertTableSQL, statement, rowClass, rowLesson, rowDOTW, myExcelSheet, m, school, "_vrm");
//        return yourClass;
//    }
// сделай свой класс с полями XSSFRow rowDOTW и XSSFRow rowLesson чтобы возвращать в методе нужную инфу
// вызываешь его так yourClass = changeMethodName(rowDOTW, rowLesson, myExcelSheet, m, insertTableSQL,statement, rowClass, school);
// выше пример метода.
    public List<String> dbAddSchedule(String file, String school) throws Exception {

        Statement statement = connects().createStatement();
        System.out.println(file);
        XSSFWorkbook myExcelBook = new XSSFWorkbook(file);
        XSSFSheet myExcelSheet = myExcelBook.getSheetAt(0);
        XSSFRow rowDOTW = null;
        String insertTableSQL = "";
        findNumberDOTW(myExcelSheet);
        XSSFRow rowLesson = myExcelSheet.getRow(kk.get(DOTW[0]));
        XSSFRow rowClass = myExcelSheet.getRow(kk.get(DOTW[0]) - 1);
        XSSFRow rowClassw = myExcelSheet.getRow(kk.get(DOTW[0]) - 1);
        for (int m = 0; m < 6; m++) {
            rowDOTW = myExcelSheet.getRow(kk.get(DOTW[m]));
            rowLesson = myExcelSheet.getRow(kk.get(DOTW[m]));
            addOneDay(insertTableSQL, statement, rowClass, rowLesson, rowDOTW, myExcelSheet, m, school, "_vrm");
        }
        if (myExcelBook.getSheetAt(1) != null && isMergedRegion(myExcelSheet, (kk.get(DOTW[0]) - 2), 2)) {
            myExcelSheet = myExcelBook.getSheetAt(1);
            findNumberDOTW(myExcelSheet);
            rowClass = myExcelSheet.getRow(kk.get(DOTW[0]) - 2);
            rowClassw = myExcelSheet.getRow(kk.get(DOTW[0]) - 1);
            for (int m = 0; m < 6; m++) {
                rowDOTW = myExcelSheet.getRow(kk.get(DOTW[m]));
                rowLesson = myExcelSheet.getRow(kk.get(DOTW[m]));
                addOneDay(insertTableSQL, statement, rowClass, rowClassw, rowLesson, rowDOTW, myExcelSheet, m, school, "_vrm");
            }
        }
        statement.executeUpdate("DELETE FROM schedule_vrm where lesson=''");
        //newSchedules = getNewScheduleFromSchool(school);
        myExcelSheet = myExcelBook.getSheetAt(0);
        findNumberDOTW(myExcelSheet);
        rowLesson = myExcelSheet.getRow(kk.get(DOTW[0]));
        rowClass = myExcelSheet.getRow(kk.get(DOTW[0]) - 1);
        statement.executeUpdate("DELETE FROM schedule where school='" + school + "'");
        for (int m = 0; m < 6; m++) {
            rowDOTW = myExcelSheet.getRow(kk.get(DOTW[m]));
            rowLesson = myExcelSheet.getRow(kk.get(DOTW[m]));
            addOneDay(insertTableSQL, statement, rowClass, rowLesson, rowDOTW, myExcelSheet, m, school, "");
        }
        if (myExcelBook.getSheetAt(1) != null && isMergedRegion(myExcelSheet, (kk.get(DOTW[0]) - 2), 2)) {
            myExcelSheet = myExcelBook.getSheetAt(1);
            findNumberDOTW(myExcelSheet);
            rowClass = myExcelSheet.getRow(kk.get(DOTW[0]) - 2);
            for (int m = 0; m < 6; m++) {
                rowDOTW = myExcelSheet.getRow(kk.get(DOTW[m]));
                rowLesson = myExcelSheet.getRow(kk.get(DOTW[m]));
                addOneDay(insertTableSQL, statement, rowClass, rowClassw, rowLesson, rowDOTW, myExcelSheet, m, school, "");
            }
        }
        statement.executeUpdate("DELETE FROM schedule_vrm");
        statement.executeUpdate("DELETE FROM schedule where lesson=''");


        statement.close();
        myExcelBook.close();
        connects().close();
        return newSchedules;
    }

    public void findNumberDOTW(XSSFSheet myExcelSheet) {
        int x = 0, p = 0;
        for (int z = 0; z < 6; z++) {
            XSSFRow rowDOTW = myExcelSheet.getRow(x);
            while (!rowDOTW.getCell(0).getStringCellValue().equalsIgnoreCase(DOTW[z])) {
                rowDOTW = myExcelSheet.getRow(x);
                x++;
            }
            x--;
            if (z == 0) {
                p = x;
            }
            kk.put(DOTW[z], x);
            x++;
        }
    }

    public static boolean isMergedRegion(XSSFSheet sheet, int row, int column) {
        final int sheetMergeCount = sheet.getNumMergedRegions();
        CellRangeAddress ca;
        for (int i = 0; i < sheetMergeCount; i++) {
            ca = sheet.getMergedRegion(i);
            if (row >= ca.getFirstRow() && row <= ca.getLastRow() && column >= ca.getFirstColumn() && column <= ca.getLastColumn()) {
                return true;
            }
        }
        return false;
    }

    public void addOneDay(String insertTableSQL, Statement statement, XSSFRow rowClass, XSSFRow rowClassw, XSSFRow rowLesson, XSSFRow rowDOTW, XSSFSheet myExcelSheet, int dd, String school, String vrm) throws SQLException {
        int y = kk.get(DOTW[dd]), cellLess1 = 2, cellClass1 = 2, cellClassw1 = 2, j = 2, ok = 1;
        while (j < rowDOTW.getLastCellNum()) {//проход по столбцам
            ok = 1;//счетчик schedule_id в бд
            if (dd != 5) {//пока день недели не равен субботе
                while (y < kk.get(DOTW[dd + 1])) {//проход по строкам
                    try {
                        Cell cellLess = rowLesson.getCell(cellLess1);//получаем значение ячейки урока
                        Cell cellClass = rowClass.getCell(cellClass1);//получаем значение ячейки класса
                        Cell cellClassw = rowClassw.getCell(cellClassw1);
                        Cell cellDOTW = rowDOTW.getCell(0);

                        insertTableSQL = "INSERT INTO schedule" + vrm + " (SCHEDULE_ID, LESSON_ID, LESSON, CLASS, DOTW,school) " +
                                "VALUES" + "(" + h + "," + ok + ", '"
                                + cellLess + "', '"
                                + cellClass + "" + cellClassw + "', '"
                                + cellDOTW + "','" + school + "');";
                        statement.executeUpdate(insertTableSQL);
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                    y++;// увеличиваем значение строки
                    ok++;// увеличиваем значение номера урока
                    h++;// увеличиваем значение schedule_id
                    rowLesson = myExcelSheet.getRow(y);// записываем в строку урока следующую строку
                }
            } else {//если день недели суббота
                while (isMergedRegion(myExcelSheet, y, 0)) {//пока строка является обьединенной проходим
                    try {
                        Cell cellLess = rowLesson.getCell(cellLess1);
                        Cell cellClass = rowClass.getCell(cellClass1);
                        Cell cellClassw = rowClassw.getCell(cellClassw1);
                        Cell cellDOTW = rowDOTW.getCell(0);

                        insertTableSQL = "INSERT INTO schedule" + vrm + " (SCHEDULE_ID, LESSON_ID, LESSON, CLASS, DOTW,school) " +
                                "VALUES" + "(" + h + "," + ok + ", '"
                                + cellLess + "', '"
                                + cellClass + "" + cellClassw + "', '"
                                + cellDOTW + "','" + school + "');";
                        statement.executeUpdate(insertTableSQL);
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                    y++;
                    ok++;
                    h++;
                    rowLesson = myExcelSheet.getRow(y);
                }
            }
            j++;
            cellLess1++;
            cellClass1++;
            cellClassw1++;
            y = kk.get(DOTW[dd]);
            rowLesson = myExcelSheet.getRow(y);
        }
    }

// TODO че это, почему тот же метод описал второй раз? отличия минимальны, я как понял одного столбца просто нету, перегрузку методов тогда используй а не просто два одинаковых пиши
// если че перегрузка метода для примера
//    public void overMethod(int a, int b) {
//        a += b;
//    }
//    public void overMethod(int a) {
//        overMethod(a, 10);
//    }
//      у тебя дубликат логики просто без значения, это насклько я понял
//    public void overMethod(int a) {
//        a += 10;
//    }


    public void addOneDay(String insertTableSQL, Statement statement, XSSFRow rowClass, XSSFRow rowLesson, XSSFRow rowDOTW, XSSFSheet myExcelSheet, int dd, String school, String vrm) throws SQLException {
        int y = kk.get(DOTW[dd]), cellLess1 = 2, cellClass1 = 2, j = 2, ok = 1;
        while (j < rowDOTW.getLastCellNum()) {//проход по столбцам
            ok = 1;//счетчик schedule_id в бд
            if (dd != 5) {//пока день недели не равен субботе
                while (y < kk.get(DOTW[dd + 1])) {//проход по строкам
                    try {
                        Cell cellLess = rowLesson.getCell(cellLess1);//получаем значение ячейки урока
                        Cell cellClass = rowClass.getCell(cellClass1);//получаем значение ячейки класса
                        Cell cellDOTW = rowDOTW.getCell(0);

                        insertTableSQL = "INSERT INTO schedule" + vrm + " (SCHEDULE_ID, LESSON_ID, LESSON, CLASS, DOTW,school) " +
                                "VALUES" + "(" + h + "," + ok + ", '"
                                + cellLess + "', '"
                                + cellClass + "', '"
                                + cellDOTW + "','" + school + "');";
                        statement.executeUpdate(insertTableSQL);
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                    y++;// увеличиваем значение строки
                    ok++;// увеличиваем значение номера урока
                    h++;// увеличиваем значение schedule_id
                    rowLesson = myExcelSheet.getRow(y);// записываем в строку урока следующую строку
                }
            } else {//если день недели суббота
                while (isMergedRegion(myExcelSheet, y, 0)) {//пока строка является обьединенной проходим
                    try {
                        Cell cellLess = rowLesson.getCell(cellLess1);
                        Cell cellClass = rowClass.getCell(cellClass1);
                        Cell cellDOTW = rowDOTW.getCell(0);

                        insertTableSQL = "INSERT INTO schedule" + vrm + " (SCHEDULE_ID, LESSON_ID, LESSON, CLASS, DOTW,school) " +
                                "VALUES" + "(" + h + "," + ok + ", '"
                                + cellLess + "', '"
                                + cellClass + "', '"
                                + cellDOTW + "','" + school + "');";
                        statement.executeUpdate(insertTableSQL);
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                    y++;
                    ok++;
                    h++;
                    rowLesson = myExcelSheet.getRow(y);
                }
            }
            j++;
            cellLess1++;
            cellClass1++;
            y = kk.get(DOTW[dd]);
            rowLesson = myExcelSheet.getRow(y);
        }
    }


    public String getUserPhone(long chatId) throws Exception {
        String userPhone = "";

        Statement statement = connects().createStatement();
        ResultSet rs = statement.executeQuery("select user_numberphone from users where user_id=" + chatId);
        rs.next();
        userPhone = rs.getString("user_numberphone");
        rs.close();
        statement.close();
        connects().close();
        return userPhone;
    }
}