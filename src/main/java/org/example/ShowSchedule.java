package org.example;

import javax.swing.plaf.nimbus.State;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShowSchedule {

    public ShowSchedule() throws SQLException {
    }

    public void showSchedule(String userClass, long chatId, String school, Statement statement) throws Exception {
//        Class.forName("org.postgresql.Driver");
        String zapros = "SELECT schedule_id, lesson_id, lesson, class, dotw " +
                "FROM schedule where class = '" + userClass + "' and lesson <> 'null' and lesson <> ''and school='" + school + "' order by schedule_id";
        Bot bot = new Bot();
        ResultSet rs = statement.executeQuery(zapros);
        String textForUser = "Расписание для " + spaceBetweenClassAndProf(userClass) + ": \nПонедельник\n" + " |№ урока| Урок\n";
        while (rs.next() && rs != null) {
            while (rs.getString("dotw").equalsIgnoreCase("ПОНЕДЕЛЬНИК") && rs != null) {
                textForUser += "| " + rs.getString("lesson_id") + " | " +
                        rs.getString("lesson") + " \n";
                rs.next();
            }
            textForUser += "Вторник\n" + " |№ урока| Урок\n";
            while (rs.getString("dotw").equalsIgnoreCase("ВТОРНИК") && rs != null) {
                textForUser += "| " + rs.getString("lesson_id") + " | " +
                        rs.getString("lesson") + " \n";
                rs.next();
            }
            textForUser += "Среда\n" + " |№ урока| Урок\n";
            while (rs.getString("dotw").equalsIgnoreCase("СРЕДА") && rs != null) {
                textForUser += "| " + rs.getString("lesson_id") + " | " +
                        rs.getString("lesson") + " \n";
                rs.next();
            }
            textForUser += "Четверг\n" + " |№ урока|\n";
            while (rs.getString("dotw").equalsIgnoreCase("ЧЕТВЕРГ") && rs != null) {
                textForUser += "| " + rs.getString("lesson_id") + " | " +
                        rs.getString("lesson") + " \n";
                rs.next();
            }
            textForUser += "Пятница\n" + " |№ урока| Урок\n";
            while (rs.getString("dotw").equalsIgnoreCase("ПЯТНИЦА") && rs != null) {
                textForUser += "| " + rs.getString("lesson_id") + " | " +
                        rs.getString("lesson") + " \n";
                rs.next();
            }
            textForUser += "Суббота\n" + " |№ урока| Урок\n";
            while (rs.getString("dotw").equalsIgnoreCase("СУББОТА") && rs != null) {
                textForUser += "| " + rs.getString("lesson_id") + " | " +
                        rs.getString("lesson") + " \n";
                if (!rs.next()) {
                    break;
                }
            }
            break;
        }
        if (chatId == 743234635 || chatId == 1188351220 || chatId == 5959939548L || chatId == 5471231917L) {
            bot.universalMethodForSend(chatId, textForUser,
                    new String[]{"Добавить расписание", "Узнать расписание", "Узнать свое расписание", "Настройки"});
        } else {
            bot.universalMethodForSend(chatId, textForUser,
                    new String[]{"Узнать расписание", "Узнать свое расписание", "Настройки"});
        }
        rs.close();
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

    public int getClassNumb(String school, Statement statement) throws Exception {
        String numbs = "";

        String zapros = "SELECT Distinct class FROM schedule where class <> '' AND school='" + school + "'";
        ResultSet rs = statement.executeQuery(zapros);
        int i = 0, value = 1, value2 = 1;
        while (rs.next() && rs != null) {
            numbs = rs.getString("class");
            value = Integer.parseInt(numbs.replaceAll("[^0-9]", ""));
            if (value2 > value) {
                value = value2;
            }
            value2 = value;
        }
        rs.close();
        return value;
    }

    public String[] getClassLetter(String numbClass, Statement statement) throws Exception {
        List<String> letss = new ArrayList<>();
        String zapros = "SELECT Distinct class FROM schedule where class like '" + numbClass + "%' group by class order by class";
        ResultSet rs = statement.executeQuery(zapros);
        while (rs.next() && rs != null) {
            if (Integer.parseInt(numbClass) < 10) {
                letss.add(rs.getString("class").replaceAll("[0-9]", ""));
            } else {
                letss.add(rs.getString("class").replaceAll("\\d{2}[A-Za-z]{1}", "").substring(2, 3));
            }

        }
        List<String> lessClass = letss.stream()
                .collect(Collectors.toSet())
                .stream()
                .collect(Collectors.toList());
        int x = 0;
        String lets[] = new String[lessClass.size()];
        for (String temp : lessClass) {
            lets[x] = temp;
            x++;
        }
        rs.close();
        return lets;
    }

    public String[] getClassProf(String numbClass, Statement statement) throws Exception {
        String lets[] = new String[10];

        String zapros = "SELECT Distinct class FROM schedule where class like '" + numbClass + "%' group by class order by class";
        ResultSet rs = statement.executeQuery(zapros);
        int x = 0;
        while (rs.next() && rs != null) {
            lets[x] = rs.getString("class");
            lets[x] = lets[x].replaceAll("[0-9].$", "");
            x++;
        }
        rs.close();
        return lets;
    }

    public String showSchedule(long chatId, String klas, String dotatw, String school, Statement statement) throws Exception {

        String zapros = "SELECT schedule_id, lesson_id, lesson, class, dotw,school " +
                "FROM schedule where class = '" + klas + "' and lesson <> 'null' and school='" + school + "' and lesson <> '' and dotw ='" + dotatw.toUpperCase() + "' order by schedule_id";
        Bot bot = new Bot();
        ResultSet rs = statement.executeQuery(zapros);
        String textForUser = "Расписание для " + spaceBetweenClassAndProf(klas) + ": \n" + " |№ урока| Урок\n";
        while (rs.next() && rs != null) {
            textForUser += "| " + rs.getString("lesson_id") + " | " +
                    rs.getString("lesson") + "  " + "\n";
        }
        rs.close();
        return textForUser;
    }
}