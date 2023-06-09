package org.example;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class DBConnect {
    String[] DOTW = new String[]{"ПОНЕДЕЛЬНИК", "ВТОРНИК", "СРЕДА",
            "ЧЕТВЕРГ", "ПЯТНИЦА", "СУББОТА"};
    Map<String, Integer> kk = new HashMap<>();
    int h = 1;
    List<String> newSchedules = new ArrayList<>();

    public Integer getPage(Long chatId, Statement statement) throws SQLException, IOException {
        int School = 1;
        ResultSet rs = statement.executeQuery("select page from users where user_id=" + chatId);
        rs.next();
        School = rs.getInt("page");
        rs.close();
        return School;
    }

    public void setPage(Long chatId, int page, Statement statement) throws SQLException, IOException {
        statement.executeUpdate("UPDATE users set page = " + page + " where user_id=" + chatId);
    }

    public List<String> getAllSchool(Statement statement) throws SQLException, IOException {
        List<String> masSchool = new ArrayList<>();
        ResultSet rs = statement.executeQuery("select school from school");
        while (rs.next()) {
            masSchool.add(rs.getString("school"));
        }
        rs.close();
        return masSchool;
    }

    public List<String> getAllClass(String school, Statement statement) throws SQLException, IOException {
        List<String> masClass = new ArrayList<>();
        ResultSet rs = statement.executeQuery("SELECT distinct class" +
                " from schedule" +
                " where school='" + school + "' order by class");
        while (rs.next()) {
            masClass.add(spaceBetweenClassAndProf(rs.getString("class")));
        }
        System.out.println(masClass.get(0));
        rs.close();
        return masClass;
    }

    public String spaceBetweenClassAndProf(String stroka) {
        String strClass = stroka.substring(0, 2);
        if (strClass.equals("11")) {
            strClass = stroka.substring(0, 3);
            String strProf = stroka.substring(3);
            stroka = strClass + " " + strProf;
        }
        return stroka;
    }

    public String getSchool(Long chatId, Statement statement) throws Exception {
        String School = "";
        ResultSet rs = statement.executeQuery("select user_school from users where user_id=" + chatId);
        rs.next();
        School = rs.getString("user_school");
        rs.close();
        return School;
    }

    public String[] getClassProfile(Long chatId, String numberAndletterclass, Statement statement) throws SQLException, IOException {
        List<String> listClassProf = new ArrayList<>();
        ResultSet rs = statement.executeQuery("SELECT distinct  class FROM schedule where class like '" + numberAndletterclass + "%'");
        while (rs.next()) {
            listClassProf.add(rs.getString("class").substring(3));
        }
        String[] listProf = new String[listClassProf.size()];
        for (int x = 0; x < listClassProf.size(); x++) {
            listProf[x] = listClassProf.get(x);
        }
        rs.close();
        return listProf;
    }

//    public String[] getClassLetter(Long chatId, String numberClass) throws SQLException {
//        List<String> listClassLetter = new ArrayList<>();
//        //Statement statement = connection.createStatement();
//        ResultSet rs = statement.executeQuery("SELECT distinct  class FROM schedule where class like '" + numberClass + "%'");
//        while (rs.next()) {
//            listClassLetter.add(rs.getString("class").substring(2, 3));
//        }
//        String[] ClassLetters = new String[listClassLetter.size()];
//        for (int x = 0; x < listClassLetter.size(); x++) {
//            ClassLetters[x] = listClassLetter.get(x);
//        }
//        rs.close();
//       // statement.close();
//       //connects().close();
//        return ClassLetters;
//    }

    public String getSubToSchedule(Long chatId, Statement statement) throws Exception {
        String Sub = "";
        ResultSet rs = statement.executeQuery("select subtoschedule from  users where user_id=" + chatId);
        if (!rs.next()) {
            rs.close();
            return null;
        } else {
            Sub = rs.getString("subtoschedule");
            rs.close();
        }
        return Sub;
    }

    public List<String> getAllUsers(Statement statement) throws Exception {
        List<String> allUser = new ArrayList<>();
        ResultSet rs = statement.executeQuery("select user_id from users");
        while (rs.next()) {
            allUser.add(rs.getString("user_id"));
        }
        rs.close();
        return allUser;
    }

    public List<String> getUserNewScheduleInSchool(String newScheduleSchool, String newScheduleClass, Statement statement) throws Exception {
        List<String> masSchool = new ArrayList<>();
        ResultSet rs = statement.executeQuery("select user_id from users where user_school='" + newScheduleSchool + "' and user_class='" + newScheduleClass + "'");
        int x = 0;
        while (rs.next()) {
            masSchool.add(rs.getString("user_id"));
            x++;
        }
        rs.close();
        return masSchool;
    }


    public void setUserData(Long chatId, String data, String typeData, Statement statement) throws Exception {
        statement.executeUpdate("UPDATE users set " + typeData + " = '" + data + "' where user_id=" + chatId);
    }

    public boolean setChildCode(Long chatId, String code, Statement statement) throws Exception {
        ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM child_user WHERE user_id ='" + chatId + "' AND child_code='" + code + "'");
        int count = rs.getInt(1);
        if (count == 0) {
            rs = statement.executeQuery("select sum_debt from debt where REPLACE(child_code, ' ', '')='" + code + "'");
            while (rs.next()) {
                statement.executeUpdate("insert into child_user (user_id,child_code,sum_debt) values (" + chatId + ",'" + code + "','"
                        + rs.getString("sum_debt") + "')");
            }
            rs.close();
            return true;
        } else {
            rs.close();
            return false;
        }

    }


    //TODO есть мнение что 4 метода ниже идут в один, внезапно. просто еще три строки передаешь где нужные поля, таблицы, и поля для выборки
    public String getState(Long chatId, Statement statement) throws Exception {
        String state = "";
        ResultSet rs = statement.executeQuery("select user_state from users where user_id=" + chatId);
        rs.next();
        state = rs.getString("user_state");
        rs.close();
        return state;
    }

    public String getGlobalState(Long chatId, Statement statement) throws Exception {
        String state = "";
        ResultSet rs = statement.executeQuery("select global_state from users where user_id=" + chatId);
        rs.next();
        state = rs.getString("global_state");
        rs.close();
        return state;
    }

    public String getChatId(Long chatId, Statement statement) throws Exception {
        String user = "";
        ResultSet rs = statement.executeQuery("select user_id from users where user_id=" + chatId);
        if (!rs.next()) {
            rs.close();
            return null;
        } else {
            user = rs.getString("user_id");
            rs.close();
        }
        return user;
    }

    public String getLetter(Long chatId, Statement statement) throws Exception {
        String letter = "";
        ResultSet rs = statement.executeQuery("select user_selectwletter from users where user_id=" + chatId);
        if (rs.next()) {
            letter = rs.getString("user_selectwletter");
        } else {
            return null;
        }
        rs.close();
        return letter;
    }

    public String getLetterAndNumberClass(Long chatId, Statement statement) throws Exception {
        String letter = "";
        ResultSet rs = statement.executeQuery("select user_selectwletter, user_selectclass from users where user_id=" + chatId);
        if (rs.next()) {
            letter = rs.getString("user_selectclass");
            letter += rs.getString("user_selectwletter");
        } else {
            return null;
        }
        rs.close();
        return letter;
    }

    public String getClass(Long chatId, Statement statement) throws Exception {
        String clas = "";
        ResultSet rs = statement.executeQuery("select user_class from users where user_id=" + chatId);
        rs.next();
        clas = rs.getString("user_class");
        rs.close();
        return clas;
    }

    public String getSelectClass(Long chatId, Statement statement) throws Exception {
        String selectclas = "";
        ResultSet rs = statement.executeQuery("select user_selectclass, user_selectwletter from users where user_id=" + chatId);
        rs.next();
        selectclas = rs.getString("user_selectclass");
        rs.close();
        return selectclas;
    }

    public void createUser(Long chatId, Statement statement) throws Exception {
        statement.executeUpdate("INSERT INTO users (user_id, user_state, user_selectclass, user_selectwletter) values ("
                + chatId + ", 'create','','')");
    }

    public List<String> getNewScheduleFromSchool(String school, Statement statement) throws Exception {
        List<String> ScheduleNew = new ArrayList<>();
        ResultSet rs = statement.executeQuery("Select lesson_id,lesson,class,dotw, school from schedule_vrm except Select lesson_id,lesson,class,dotw, school from schedule where school='" + school + "'");
        while (rs.next()) {
            ScheduleNew.add("{" + rs.getString("class") + " в " + rs.getString("dotw") +
                    "} урок - (№" + rs.getString("lesson_id") + " : " + rs.getString("lesson") + ")");
        }
        rs.close();
        return ScheduleNew;
    }

    public void deleteAccount(Long chatId, Statement statement) throws Exception {
        statement.executeUpdate("DELETE FROM users where user_id='" + chatId + "'");
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


    public void dbAddDebt(String file, Statement statement) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(new File(file));
        HSSFWorkbook myExcelBook = new HSSFWorkbook(fileInputStream);
        HSSFSheet myExcelSheet = myExcelBook.getSheetAt(0);
        HSSFRow rowDOTW = null;
        String insertTableSQL = "";
        int aIndex = -1;
        int cIndex = -1;
        Iterator<Row> iterator = myExcelSheet.iterator();
        if (iterator.hasNext()) {
            Row row = iterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext() && (aIndex == -1 || cIndex == -1)) {
                Cell cell = cellIterator.next();
                if (cell.getStringCellValue().trim().equals("РебенокКод")) {
                    aIndex = cell.getColumnIndex();
                }
                if (cell.getStringCellValue().trim().equals("СуммаОстатокДт")) {
                    cIndex = cell.getColumnIndex();
                }
            }
        }
        List<String> aList = new ArrayList<>();
        List<String> cList = new ArrayList<>();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            if (row.getCell(aIndex) != null) {
                aList.add(row.getCell(aIndex).toString());
            } else {
                aList.add("");
            }

            if (row.getCell(cIndex) != null) {
                cList.add(row.getCell(cIndex).toString());
            } else {
                cList.add("");
            }
        }
        statement.executeUpdate("DELETE FROM debt");
        for (int x = 0; x < aList.size(); x++) {
            statement.executeUpdate("INSERT INTO debt (id, child_code, sum_debt) values (" + x + ",'" + aList.get(x) + "','" + cList.get(x) + "');");
        }
        myExcelBook.close();
    }

    public List<String> dbAddSchedule(String file, String school, Statement statement) throws Exception {

        //Statement statement = connection.createStatement();
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
        myExcelBook.close();
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

    public List<String> getDebt(Long chatId, Statement statement) throws SQLException {
        List<String> debt = new ArrayList<>();
        ResultSet rs = statement.executeQuery("select sum_debt from child_user where user_id=" + chatId);
        while (rs.next()) {
            debt.add(rs.getString("sum_debt"));
        }
        rs.close();
        return debt;
    }

    public void deleteCodeChild(Long chatId, String code, Statement statement) throws SQLException {
        statement.executeUpdate("delete from child_user where child_code ='" + code + "' AND user_id = " + chatId);
    }

    public List<String> getCodeChild(Long chatId, Statement statement) throws SQLException {
        List<String> code = new ArrayList<>();
        ResultSet rs = statement.executeQuery("select child_code from child_user where user_id=" + chatId);
        while (rs.next()) {
            code.add(rs.getString("child_code"));
        }
        rs.close();
        return code;
    }

    public List<String> getDebtFromKSHP(String newCode, Statement statement) throws SQLException {
        List<String> debt = new ArrayList<>();
        ResultSet rs = statement.executeQuery("select sum_debt,child_code from debt where REPLACE(child_code, ' ', '')='" + newCode.trim() + "'");
        while (rs.next()) {
            debt.add(rs.getString("sum_debt"));
            debt.add(rs.getString("child_code"));
        }
        rs.close();
        return debt;
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

    public String getUserPhone(long chatId, Statement statement) throws Exception {
        String userPhone = "";

        ResultSet rs = statement.executeQuery("select user_numberphone from users where user_id=" + chatId);
        rs.next();
        userPhone = rs.getString("user_numberphone");
        return userPhone;
    }
}