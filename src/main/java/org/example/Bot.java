package org.example;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bot extends TelegramLongPollingBot {

    final ConfigBot configBot = new ConfigBot();
    String txt = "";
    String[] DOTW = new String[]{"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};//TODO игде не используется, удаляем? по идеи его на 458 и 462 строке можно юзать
    String[] rangeElClas = new String[]{"1", "2", "3", "4"};
    String[] rangeMidClas = new String[]{"5", "6", "7", "8"};
    String[] rangeHigClas = new String[]{"9", "10", "11"};
    List<String> nwSchedule = new ArrayList<>();
    Connection connection = DriverManager.getConnection("jdbc:sqlite:botSchedule.db");
    private final DBConnect dbConnect = new DBConnect();
    private final ShowSchedule ss = new ShowSchedule();
    Statement statement = connection.createStatement();

    public Bot() throws SQLException {
    }

    public void TimerTask() {
        Date date = new Date();
        try {
            if (date.getDay() != 0) {
                if (date.getHours() == 16 && date.getMinutes() == 12) {
                    String tomorrow = DayOfWeek.byDayOfWeek(date.getDay() + 1);
                    String tomorrowEnd = DayOfWeek.byDayOfWeekEnds(date.getDay() + 1);
                    String schoolar = "", classar = "";
                    for (int x = 0; x < dbConnect.getAllUsers(statement).size(); x++) {
                        System.out.println(dbConnect.getSubToSchedule(Long.valueOf(dbConnect.getAllUsers(statement).get(x)), statement));
                        if (dbConnect.getSubToSchedule(Long.valueOf(dbConnect.getAllUsers(statement).get(x)), statement) != null && dbConnect.getSubToSchedule(Long.valueOf(dbConnect.getAllUsers(statement).get(x)), statement).equalsIgnoreCase("true")) {
                            schoolar = dbConnect.getSchool(Long.valueOf(dbConnect.getAllUsers(statement).get(x)), statement);
                            classar = dbConnect.getClass(Long.valueOf(dbConnect.getAllUsers(statement).get(x)), statement);
                            sendJustMessage(Long.valueOf(dbConnect.getAllUsers(statement).get(x)), "Расписание на " + tomorrowEnd);
                            showSheduleEveryDay(tomorrow, schoolar, classar, Long.valueOf(dbConnect.getAllUsers(statement).get(x)));
                            userOrAdmin(Long.valueOf(dbConnect.getAllUsers(statement).get(x)));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    @Override
    public String getBotUsername() {
        try {
            return configBot.forBotAndDB()[0];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotToken() {
        try {
            return configBot.forBotAndDB()[1];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void rangeClass(long chatId, String range, String[] rangeVal, String one) {
        try {
            dbConnect.setUserData(chatId, range + one, "user_state", statement);
            universalMethodForSend(chatId, "Выберите номер класса:", rangeVal);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void changeStateSub(long chatId, String bool, Update update, String messegeChanges) {
        try {
            sendJustMessage(chatId, messegeChanges);
            setsUserData(chatId, bool, "subtoschedule", "default", "user_state");
            userOrAdmin(chatId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setsUserData(long chatId, String selectFor, String state, String data, String typeData) {
        try {
            dbConnect.setUserData(chatId, selectFor, state, statement);
            dbConnect.setUserData(chatId, data, typeData, statement);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    public void sendPhoto(Long chatId) throws Exception {
//        dbConnect.setUserData(chatId, "firstReaction", "global_state",statement);
//        SendPhoto sendPhoto = new SendPhoto();
//        sendPhoto.setChatId(chatId + "");
//        sendPhoto.setPhoto(new InputFile("https://ibb.co/bdJg3KD"));
//        try {
//            execute(sendPhoto);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void onUpdateReceived(Update update) {
        long chatId = update.getMessage().getChatId();
//        try {
//            dbConnect.setUserData(chatId, "Main", "global_state", statement);
//            dbConnect.setUserData(chatId, "default", "user_state", statement);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        System.out.println(update.getMessage().getChat().getFirstName() + " - " + update.getMessage().getChat().getUserName());
        try {
            if (dbConnect.getChatId(chatId, statement) == null) {
                dbConnect.createUser(chatId, statement);
                dbConnect.setUserData(chatId, "firstReaction", "global_state", statement);
                sendJustMessage(chatId, "Добро пожаловать!");
                requestNumberPhone(chatId);
            } else if (update.getMessage().hasText()) {
//                if (update.getMessage().getText().equalsIgnoreCase("/start")) {
                if (dbConnect.getGlobalState(chatId, statement).equals("regPage") && !dbConnect.getState(chatId, statement).equals("regPage_2") && !dbConnect.getState(chatId, statement).equals("regPage_3")) {
                    dbConnect.setUserData(chatId, "firstReaction", "global_state", statement);
                    sendJustMessage(chatId, "Добро пожаловать!");
                    requestNumberPhone(chatId);
                }
//                }
            }
            System.out.println(update.getMessage().getText());
            if (dbConnect.getGlobalState(chatId, statement).equals("firstReaction")) {
                setsUserData(chatId, "regPage", "global_state", "regPage_1", "user_state");
            }

            if (dbConnect.getGlobalState(chatId, statement).equals("regPage")) {
                switch (dbConnect.getState(chatId, statement)) {
                    case "regPage_1":
                        if (update.getMessage().getContact() != null &&
                                verificationNumberPhone(update.getMessage().getContact().getPhoneNumber().toString(), chatId)) {
                            setsUserData(chatId, update.getMessage().getContact().getPhoneNumber().toString(), "user_numberphone", "regPage_2", "user_state");
                            sendSchoolInCity(chatId, "Выберите школу: \n# <u><b><i>Кнопки расположены под чатом</i></b></u> #", 1);
                            dbConnect.setPage(chatId, 1, statement);
                        }
                        break;
                    case "regPage_2":
                        switch (update.getMessage().getText()) {
                            case "->":
                                dbConnect.setUserData(chatId, "regPage_2", "user_state", statement);
                                dbConnect.setPage(chatId, dbConnect.getPage(chatId, statement) + 1, statement);
                                sendSchoolInCity(chatId, "Выберите школу: \n# <u><b><i>Кнопки расположены под чатом</i></b></u> #", dbConnect.getPage(chatId, statement));
                                break;
                            case "<-":
                                dbConnect.setUserData(chatId, "regPage_2", "user_state", statement);
                                dbConnect.setPage(chatId, dbConnect.getPage(chatId, statement) - 1, statement);
                                sendSchoolInCity(chatId, "Выберите школу: \n# <u><b><i>Кнопки расположены под чатом</i></b></u> #", dbConnect.getPage(chatId, statement));
                                break;
                            default:
                                for (String str : dbConnect.getAllSchool(statement)) {
                                    if (update.getMessage().getText().equals(str)) {
                                        dbConnect.setUserData(chatId, "regPage_3", "user_state", statement);
                                        sendClassInSchool(chatId, "Выберите класс:", 1, update.getMessage().getText());
                                        dbConnect.setUserData(chatId, update.getMessage().getText(), "user_school", statement);
                                        dbConnect.setPage(chatId, 1, statement);
                                        break;
                                    } else {
                                        dbConnect.setUserData(chatId, "regPage_2", "user_state", statement);
                                        sendSchoolInCity(chatId, "Выберите школу: + \n# <u><b><i>Кнопки расположены под чатом</i></b></u> #", 1);
                                        dbConnect.setPage(chatId, 1, statement);
                                        break;
                                    }
                                }
                                break;

                        }
                        break;
                    case "regPage_3":
                        if (update.getMessage().getText() != null) {
                            switch (update.getMessage().getText()) {
                                case "->":
                                    dbConnect.setUserData(chatId, "regPage_3", "user_state", statement);
                                    dbConnect.setPage(chatId, dbConnect.getPage(chatId, statement) + 1, statement);
                                    sendClassInSchool(chatId, "Выберите класс:", dbConnect.getPage(chatId, statement), dbConnect.getSchool(chatId, statement));
                                    break;
                                case "<-":
                                    dbConnect.setUserData(chatId, "regPage_3", "user_state", statement);
                                    dbConnect.setPage(chatId, dbConnect.getPage(chatId, statement) - 1, statement);
                                    sendClassInSchool(chatId, "Выберите класс:", dbConnect.getPage(chatId, statement), dbConnect.getSchool(chatId, statement));
                                    break;
                                default:
                                    for (String str : dbConnect.getAllClass(dbConnect.getSchool(chatId, statement), statement)) {
                                        if (update.getMessage().getText().equals(str)) {
                                            setsUserData(chatId, "Main", "global_state", update.getMessage().getText().replaceFirst(" ", ""), "user_class");
//                                            dbConnect.setUserData(chatId, "Main", "global_state");
//                                            dbConnect.setUserData(chatId, update.getMessage().getText(), "user_class");
                                            dbConnect.setUserData(chatId, "defaultday1", "user_state", statement);
                                            sendJustMessage(chatId, "Регистрация прошла успешно!");
                                            userOrAdmin(chatId);
                                            break;
                                        }
                                    }
                                    if (dbConnect.getClass(chatId, statement) == null) {
                                        dbConnect.setUserData(chatId, "regPage_3", "user_state", statement);
                                        sendClassInSchool(chatId, "Выберите класс:", 1, dbConnect.getSchool(chatId, statement));
                                    }
                                    break;
                            }
                        }
                        break;
                }
            }
            if (dbConnect.getChatId(chatId, statement) != null && dbConnect.getGlobalState(chatId, statement).equals("Main")) {
                try {
                    if (!update.hasMessage()) {
                        System.out.println("test");
                    }
                    String messageText = update.getMessage().getText();
                    System.out.println(dbConnect.getState(chatId, statement));


                    if (messageText.equals("1-4") || messageText.equals("5-8") || messageText.equals("9-11")) {
                        if (messageText.equals("1-4")) {
                            rangeClass(chatId, messageText, rangeElClas, "1");
                        } else if (messageText.equals("5-8")) {
                            rangeClass(chatId, messageText, rangeMidClas, "1");
                        } else if (messageText.equals("9-11")) {
                            rangeClass(chatId, messageText, rangeHigClas, "1");
                        }

                    }

                    if (Classes.searchClass(messageText)) {
                        setsUserData(chatId, "SELECT-NUMBER1", "user_state", messageText, "user_selectclass");
                        universalMethodForSend(chatId, "Выберите букву класса:", ss.getClassLetter(messageText, statement));

                    }

                    if (SubClasses.searchSubClasses(messageText)) {
                        if (!dbConnect.getSelectClass(chatId, statement).equals("11")) {
                            setsUserData(chatId, "SELECT-LETTER1", "user_state", messageText, "user_selectwletter");
                            universalMethodForSend(chatId, "Расписание на всю неделю или на день?", new String[]{"Неделя", "День", "Вернуться"});

                        } else {
                            setsUserData(chatId, "SELECT-PROF1", "user_state", messageText, "user_selectwletter");
                            universalMethodForSend(chatId, "Выберите профиль:", dbConnect.getClassProfile(chatId, dbConnect.getLetterAndNumberClass(chatId, statement), statement));
                        }
                    }

                    if (DirectionClasses.searchDirectionClasses(messageText)) {
                        setsUserData(chatId, "SELECT-PROF21", "user_state", dbConnect.getLetter(chatId, statement) + messageText, "user_selectwletter");
                        universalMethodForSend(chatId, "Расписание на всю неделю или на день?", new String[]{"Неделя", "День", "Вернуться"});
                    }

                    if (DayOfWeek.searchDayOfWeek(messageText)) {
                        try {
                            System.out.println(messageText);
                            if (dbConnect.getState(chatId, statement).equals("SELECT-DAY-2")) {
                                sendJustMessage(chatId, ss.showSchedule(chatId, dbConnect.getClass(chatId, statement), update.getMessage().getText(), dbConnect.getSchool(chatId, statement), statement));

                            } else {
                                sendJustMessage(chatId, ss.showSchedule(chatId, dbConnect.getSelectClass(chatId, statement) + dbConnect.getLetter(chatId, statement), update.getMessage().getText(), dbConnect.getSchool(chatId, statement), statement));
                            }
                            dbConnect.setUserData(chatId, "defaultday1", "user_state", statement);
                            dbConnect.setUserData(chatId, "", "user_selectclass", statement);
                            dbConnect.setUserData(chatId, "", "user_selectwletter", statement);
                            userOrAdmin(chatId);
                        } catch (SQLException e) {
                            System.out.println(e);
                        }
                    }

                    switch (messageText) {
                        case "Добавить расписание":
                            if (AdminShedule.searchAdmin(chatId)) {
                                setsUserData(chatId, "addSchedulePage", "global_state", "addSchedulePage", "user_state");
                            }
                            break;
                        case "Узнать свое расписание":
                            try {
                                universalMethodForSend(chatId, "Расписание на всю неделю или на день?", new String[]{"Неделя", "День",});
                                dbConnect.setUserData(chatId, "vibor-svoi", "user_state", statement);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case "Узнать расписание":
                            try {
                                universalMethodForSend(chatId, "Выберите номер класса:", new String[]{"5-8", "9-11"});
                                dbConnect.setUserData(chatId, "vibor_1", "user_state", statement);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case "Настройки":
                            try {
                                universalMethodForSend(chatId, "Подписка на уведомления каждый день в 16:00", new String[]{"Подписаться на уведомления", "Убрать подписку", "Удалить аккаунт", "Вернуться"});
                                dbConnect.setUserData(chatId, "subs_schedule", "user_state", statement);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case "Удалить аккаунт":
                            if (dbConnect.getState(chatId, statement).equals("subs_schedule")) {
                                sendJustMessage(chatId, "Ваш аккаунт удален!");
                                dbConnect.deleteAccount(chatId, statement);
                            }
                        case "Подписаться на уведомления":
                            if (dbConnect.getState(chatId, statement).equals("subs_schedule")) {
                                changeStateSub(chatId, "true", update, "Подписка оформлена!");
                            }
                            break;
                        case "Убрать подписку":
                            if (dbConnect.getState(chatId, statement).equals("subs_schedule")) {
                                changeStateSub(chatId, "false", update, "Подписка отозвана!");
                            }
                            break;
                        case "Долг питания":
                            int dolg = (int) (1 + Math.random() * 20000);
                            sendJustMessage(chatId, "Ваш задолженность \n составляет: " + dolg + " рублей.");
                            userOrAdmin(chatId);
                        case "Неделя":
                            try {
                                if (dbConnect.getState(chatId, statement).equals("vibor-svoi")) {
                                    ss.showSchedule(dbConnect.getClass(chatId, statement) + dbConnect.getLetter(chatId, statement), chatId, dbConnect.getSchool(chatId, statement), statement);
                                } else {
                                    ss.showSchedule(dbConnect.getSelectClass(chatId, statement) + dbConnect.getLetter(chatId, statement), chatId, dbConnect.getSchool(chatId, statement), statement);
                                }
                                userOrAdmin(chatId);
                                dbConnect.setUserData(chatId, "default", "user_state", statement);
                                dbConnect.setUserData(chatId, "", "user_selectclass", statement);
                                dbConnect.setUserData(chatId, "", "user_selectwletter", statement);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case "Вернуться":
                            try {
                                if (dbConnect.getState(chatId, statement).equals("subs_schedule")) {
                                    userOrAdmin(chatId);
                                    dbConnect.setUserData(chatId, "default", "user_state", statement);
                                    break;
                                }
                                if (dbConnect.getState(chatId, statement).equals("SELECT-DAY-2")) {
                                    universalMethodForSend(chatId, "Расписание на всю неделю или на день?", new String[]{"Неделя", "День", "Вернуться"});
                                    dbConnect.setUserData(chatId, "vibor-svoi", "user_state", statement);
                                    break;
                                }
                                if (dbConnect.getState(chatId, statement).equals("vibor-svoi")) {
                                    userOrAdmin(chatId);
                                    dbConnect.setUserData(chatId, "default", "user_state", statement);
                                    break;
                                }
                                if (dbConnect.getState(chatId, statement).equals("SELECT-DAY")) {
                                    universalMethodForSend(chatId, "Расписание на всю неделю или на день?", new String[]{"Неделя", "День", "Вернуться"});
                                    dbConnect.setUserData(chatId, "SELECT-LETTER", "user_state", statement);
                                    break;
                                }
                                if (dbConnect.getState(chatId, statement).equals("SELECT-DAY")) {
                                    universalMethodForSend(chatId, "Расписание на всю неделю или на день?", new String[]{"Неделя", "День", "Вернуться"});
                                    dbConnect.setUserData(chatId, "SELECT-LETTER", "user_state", statement);
                                    break;
                                }
                                if (dbConnect.getState(chatId, statement).equals("SELECT-LETTER")) {
                                    universalMethodForSend(chatId, "Выберите букву класса:", ss.getClassLetter(dbConnect.getSelectClass(chatId, statement), statement));
                                    setsUserData(chatId, "SELECT-NUMBER", "user_state", "", "user_selectwletter");
                                    break;
                                }
                                if (dbConnect.getState(chatId, statement).equals("SELECT-NUMBER")) {
                                    universalMethodForSend(chatId, "Выберите номер класса:", new String[]{"5-8", "9-11"});
                                    setsUserData(chatId, "vibor_1", "user_state", "", "user_selectclass");
                                    break;
                                }
                                if (dbConnect.getState(chatId, statement).equals("1-4") || dbConnect.getState(chatId, statement).equals("5-8") || dbConnect.getState(chatId, statement).equals("9-11")) {
                                    userOrAdmin(chatId);
                                    dbConnect.setUserData(chatId, "default", "user_state", statement);
                                    break;
                                }

                            } catch (Exception e) {
                                System.out.println(e);
                            }
                            break;
                        case "День":
                            try {
                                if (dbConnect.getState(chatId, statement).equals("vibor-svoi")) {
                                    dbConnect.setUserData(chatId, "SELECT-DAY-2", "user_state", statement);
                                    universalMethodForSend(chatId, "Выберите день недели:", DOTW);
                                } else {
                                    dbConnect.setUserData(chatId, "SELECT-DAY", "user_state", statement);
                                    universalMethodForSend(chatId, "Выберите день недели:", DOTW);
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        default:
                            switch (dbConnect.getState(chatId, statement)) {
                                case "vibor-svoi":
                                    universalMethodForSend(chatId, "Расписание на всю неделю или на день?", new String[]{"Неделя", "День",});
                                    break;
                                case "vibor_1":
                                    universalMethodForSend(chatId, "Выберите номер класса:", new String[]{"5-8", "9-11"});
                                    dbConnect.setUserData(chatId, "vibor_1", "user_state", statement);
                                    break;
                                case "subs_schedule":
                                    universalMethodForSend(chatId, "Подписка на уведомления каждый день в 16:00", new String[]{"Подписаться на уведомления", "Убрать подписку", "Удалить аккаунт", "Вернуться"});
                                    dbConnect.setUserData(chatId, "subs_schedule", "user_state", statement);
                                    break;
                                case "SELECT-DAY-2":
                                    dbConnect.setUserData(chatId, "SELECT-DAY-2", "user_state", statement);
                                    universalMethodForSend(chatId, "Выберите день недели:", DOTW);
                                    break;
                                case "SELECT-DAY":
                                    dbConnect.setUserData(chatId, "SELECT-DAY", "user_state", statement);
                                    universalMethodForSend(chatId, "Выберите день недели:", DOTW);
                                    break;
                                case "1-4":
                                    rangeClass(chatId, "1-4", rangeElClas, "");
                                    break;
                                case "5-8":
                                    rangeClass(chatId, "5-8", rangeMidClas, "");
                                    break;
                                case "9-11":
                                    rangeClass(chatId, "9-11", rangeHigClas, "");
                                    break;
                                case "1-41":
                                    dbConnect.setUserData(chatId, "1-4", "user_state", statement);
                                    break;
                                case "5-81":
                                    dbConnect.setUserData(chatId, "5-8", "user_state", statement);
                                    break;
                                case "9-111":
                                    dbConnect.setUserData(chatId, "9-11", "user_state", statement);
                                    break;
                                case "SELECT-NUMBER":
                                    universalMethodForSend(chatId, "Выберите букву класса:", ss.getClassLetter(messageText, statement));
                                    break;
                                case "SELECT-PROF":
                                    universalMethodForSend(chatId, "Выберите профиль:", dbConnect.getClassProfile(chatId, dbConnect.getLetterAndNumberClass(chatId, statement), statement));
                                    break;
                                case "SELECT-NUMBER1":
                                    setsUserData(chatId, "SELECT-NUMBER", "user_state", messageText, "user_selectclass");
                                    break;
                                case "SELECT-PROF1":
                                    setsUserData(chatId, "SELECT-PROF", "user_state", messageText, "user_selectwletter");
                                    break;
                                case "SELECT-DAYS":
                                    universalMethodForSend(chatId, "Расписание на всю неделю или на день?", new String[]{"Неделя", "День",});
                                    break;
                                case "SELECT-PROF21":
                                    dbConnect.setUserData(chatId, "SELECT-DAYS", "user_state", statement);
                                    break;
                                case "SELECT-LETTER1":
                                    dbConnect.setUserData(chatId, "SELECT-DAYS", "user_state", statement);
                                    break;
                                case "defaultday":
                                    userOrAdmin(chatId);
                                    break;
                                case "defaultday1":
                                    dbConnect.setUserData(chatId, "default", "user_state", statement);
                                    break;
                                default:
                                    userOrAdmin(chatId);
                                    break;
                            }
                            break;
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
        try {
            if (dbConnect.getGlobalState(chatId, statement).equals("addSchedulePage")) {
                System.out.println(dbConnect.getState(chatId, statement));
                for (int i = 0; i < dbConnect.getAllSchool(statement).size(); i++) {
                    if (AdminShedule.searchAdmin(chatId) && dbConnect.getState(chatId, statement).equals("addSchedulePage3")) {
                        dbConnect.setUserData(chatId, "addSchedulePage3", "user_state", statement);
                    }
                    if (dbConnect.getAllSchool(statement).get(i).equals(update.getMessage().getText()) && dbConnect.getState(chatId, statement).equals("addSchedulePage")
                            && !dbConnect.getState(chatId, statement).equals("addSchedulePage3")) {
                        dbConnect.setUserData(chatId, "addSchedulePage3", "user_state", statement);
                        break;
                    } else {
                        if (!dbConnect.getState(chatId, statement).equals("addSchedulePage3")) {
                            universalMethodForSend(chatId, "Выберите школу для добавления расписания", new String[]{"Гимназия №2"});

                        }
                    }
                }
                try {
                    if (dbConnect.getState(chatId, statement).equals("addSchedulePage3")) {
                        if (update.getMessage().hasText() && update.getMessage().getText().equals("Вернуться")) {
                            userOrAdmin(chatId);
                            setsUserData(chatId, "default", "user_state", "Main", "global_state");
                        } else {
                            try {
                                uploadFile(update, chatId);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            System.out.println("txt= " + txt);
                            if (txt != "") {
                                sendJustMessage(chatId, "Добавляем расписание...");
                                try {
                                    DBConnect db = new DBConnect();
                                    nwSchedule = db.dbAddSchedule(txt, dbConnect.getSchool(chatId, statement), statement);
                                } catch (SQLException | ClassNotFoundException | IOException e) {
                                    System.out.println(e);
                                    throw new RuntimeException(e);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                dbConnect.setUserData(chatId, "Main", "global_state", statement);
                                Date date = new Date();
                                String tomorrow = DayOfWeek.byDayOfWeek(date.getDay() + 1);
                                String tomorrow2 = DayOfWeek.byDayOfWeekEnds(date.getDay() + 1);
                                if (tomorrow2.equalsIgnoreCase("Воскресенье")) {
                                    tomorrow2 = "ПОНЕДЕЛЬНИК";
                                }
                                for (int s = 0; s < dbConnect.getAllUsers(statement).size(); s++) {
                                    sendJustMessage(Long.valueOf(dbConnect.getAllUsers(statement).get(s)), "Расписание на " + tomorrow2);
                                    if (AdminShedule.searchAdmin(Long.valueOf(dbConnect.getAllUsers(statement).get(s)))) {
                                        universalMethodForSend(Long.valueOf(dbConnect.getAllUsers(statement).get(s)),
                                                ss.showSchedule(Long.valueOf(dbConnect.getAllUsers(statement).get(s)),
                                                        dbConnect.getClass(Long.valueOf(dbConnect.getAllUsers(statement).get(s)), statement),
                                                        tomorrow, dbConnect.getSchool(Long.valueOf(dbConnect.getAllUsers(statement).get(s)), statement), statement),
                                                new String[]{"Добавить расписание", "Узнать расписание", "Узнать свое расписание", "Настройки", "Долг питания"});
                                    } else {
                                        universalMethodForSend(Long.valueOf(dbConnect.getAllUsers(statement).get(s)),
                                                ss.showSchedule(Long.valueOf(dbConnect.getAllUsers(statement).get(s)),
                                                        dbConnect.getClass(Long.valueOf(dbConnect.getAllUsers(statement).get(s)), statement),
                                                        tomorrow, dbConnect.getSchool(Long.valueOf(dbConnect.getAllUsers(statement).get(s)), statement), statement),
                                                new String[]{"Узнать расписание", "Узнать свое расписание", "Настройки", "Долг питания"});
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SendMessage initMessege(ReplyKeyboardMarkup replyKeyboardMarkup, Long who, String what) {
        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        message.enableMarkdown(true);
        message.setChatId(who.toString());
        message.setText(what);
        return message;
    }

    private ReplyKeyboardMarkup initReplyKeyboardMarkup(ReplyKeyboardMarkup replyKeyboardMarkup) {
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        return replyKeyboardMarkup;
    }

    public void sendSchoolInCity(Long who, String what, int page) throws Exception {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = initMessege(replyKeyboardMarkup, who, what);
        message.setParseMode("HTML");
        initReplyKeyboardMarkup(replyKeyboardMarkup);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        if (page == 1) {
            if (dbConnect.getAllSchool(statement).size() == 3) {
                for (int i = 0; i < 3; i++) {
                    keyboardFirstRow.add(dbConnect.getAllSchool(statement).get(i));
                }
            }
            if (dbConnect.getAllSchool(statement).size() > 3) {
                for (int i = 0; i < 3; i++) {
                    keyboardFirstRow.add(dbConnect.getAllSchool(statement).get(i));
                }
                keyboardSecondRow.add("->");
            }
            if (dbConnect.getAllSchool(statement).size() == 2) {
                for (int i = 0; i < 2; i++) {
                    keyboardFirstRow.add(dbConnect.getAllSchool(statement).get(i));
                }
            }
            if (dbConnect.getAllSchool(statement).size() == 1) {
                for (int i = 0; i < 1; i++) {
                    keyboardFirstRow.add(dbConnect.getAllSchool(statement).get(i));
                }
            }
        }

        if (page == 2) {
            for (int i = 3; i < 26; i++) {
                keyboardFirstRow.add(dbConnect.getAllSchool(statement).get(i));
            }
            keyboardSecondRow.add("<-");
            keyboardSecondRow.add("->");
        }
        if (page == 3) {
            for (int i = 6; i < 9; i++) {
                keyboardFirstRow.add(dbConnect.getAllSchool(statement).get(i));
            }
            keyboardSecondRow.add("<-");
        }
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendClassInSchool(Long who, String what, int page, String school) throws Exception {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = initMessege(replyKeyboardMarkup, who, what);
        initReplyKeyboardMarkup(replyKeyboardMarkup);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        for (int i = (page - 1) * 3; i < (page - 1) * 3 + 3; i++) {
            keyboardFirstRow.add(dbConnect.getAllClass(school, statement).get(i));
        }
        if (((page - 1) * 3) != 0) {
            keyboardSecondRow.add("<-");
        }
        if (!((page - 1) * 3 + 3 == 21)) {
            keyboardSecondRow.add("->");
        }
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void universalMethodForSend(Long chatId, String chatMessage, String butMas[]) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = initMessege(replyKeyboardMarkup, chatId, chatMessage);
        message.setParseMode("HTML");
        initReplyKeyboardMarkup(replyKeyboardMarkup);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        KeyboardRow keyboardThreeRow = new KeyboardRow();
        KeyboardRow kchau = new KeyboardRow();
        keyboard.add(kchau);
        int x = 0;
        for (String str : butMas) {
            if (str.isEmpty()) {
                break;
            }
            if (x < 3) {
                keyboardFirstRow.add(str);
            } else if (x >= 3 && x < 7) {
                keyboardSecondRow.add(str);
            } else if (x >= 7 && x < 9) {
                keyboardThreeRow.add(str);
            }
            x++;
        }
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        keyboard.add(keyboardThreeRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void uploadFile(Update update, long chatId) throws Exception {
        if (update.getMessage().getDocument() == null) {
            dbConnect.setUserData(chatId, "addSchedulePage3", "user_state", statement);
            universalMethodForSend(chatId, "Отправьте файл формата Excel", new String[]{"Вернуться"});
            System.out.println("файл не отправлен!");
        } else {
            var file_name = update.getMessage().getDocument().getFileName();
            System.out.println(file_name);
            StringUtils.right(file_name, 4);
            if (StringUtils.right(file_name, 4).equals(".xml") || StringUtils.right(file_name, 5).equals(".xlsx")) {
                var file_id = update.getMessage().getDocument().getFileId();
                URL url = new URL("https://api.telegram.org/bot" + getBotToken() + "/getFile?file_id=" + file_id);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String res = in.readLine();
                JSONObject jresult = new JSONObject(res);
                JSONObject path = jresult.getJSONObject("result");
                String file_path = path.getString("file_path");
                System.out.println("Start upload");
                File localFile = new File(file_name);
                txt = file_name;
                InputStream is = new URL("https://api.telegram.org/file/bot" + getBotToken() + "/" + file_path).openStream();
                FileUtils.copyInputStreamToFile(is, localFile);
                in.close();
                is.close();
                System.out.println("Uploaded!");
            } else {
                universalMethodForSend(chatId, "Нужен файл в формате эксель",
                        new String[]{"Добавить расписание", "Узнать расписание", "Узнать свое расписание", "Настройки"});
            }
        }
    }
//    public static void readFromExcel(String file) throws IOException {
//        XSSFWorkbook myExcelBook = new XSSFWorkbook(file);
//        XSSFSheet myExcelSheet = myExcelBook.getSheetAt(0);
//        XSSFRow row = myExcelSheet.getRow(8);
//        String name = row.getCell(2).getStringCellValue();
//
//        myExcelBook.close();
//
//    }

    public void showSheduleEveryDay(String tomorrow, String schoolar, String classar, Long chatId) throws Exception {
        sendJustMessage(chatId, ss.showSchedule(chatId, classar, tomorrow, schoolar, statement));
    }

    public void sendingNewSchedule(Long chatId, List<String> newSchedule) throws Exception {//TODO пока не используется
        SendMessage message = new SendMessage();
        message.enableMarkdown(true);
        if (!newSchedule.isEmpty()) {
            for (int w = 0; w < newSchedule.size(); w++) {
                if (!newSchedule.get(w).equals("np")) {
                    String stroka = "ИЗМЕНЕНИЕ В РАСПИСАНИИ!\n";
                    int start = 1 + newSchedule.get(w).indexOf("{");
                    int end = newSchedule.get(w).indexOf(" в");
//                System.out.println(newSchedule.get(0).substring(start, end));
                    for (int i = 0; i < newSchedule.size(); i++) {
                        if (newSchedule.get(i).contains(newSchedule.get(w).substring(start, end))) {
                            stroka = stroka + newSchedule.get(i) + "\n";
                        }
                    }
                    for (int x = 0; x < dbConnect.getUserNewScheduleInSchool(dbConnect.getSchool(chatId, statement), newSchedule.get(w).substring(start, end), statement).size(); x++) {
                        message.setChatId(dbConnect.getUserNewScheduleInSchool(dbConnect.getSchool(chatId, statement), newSchedule.get(w).substring(start, end), statement).get(x));
                        if (stroka.contains(dbConnect.getClass(Long.valueOf(message.getChatId()), statement))) {
                            message.setText(stroka);
                            try {
                                execute(message);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    List<Integer> maska = new ArrayList<>();
                    for (int i = 0; i < newSchedule.size(); i++) {
                        int start2 = 1 + newSchedule.get(i).indexOf("{");
                        int end2 = newSchedule.get(i).indexOf(" в");
                        if (!newSchedule.get(i).equals("np")) {
                            if (newSchedule.get(i).substring(start2, end2).equals(newSchedule.get(w).substring(start, end))) {
                                maska.add(i);
                            }
                        }
                    }
                    for (int i = 0; i < maska.size(); i++) {
                        newSchedule.set(maska.get(i), "np");
                    }
                }
            }
        }
    }

    public void sendJustMessage(Long chatId, String msg) {
        SendMessage message = new SendMessage();
        message.enableMarkdown(true);
        message.setChatId(chatId.toString());
        message.setText(msg);
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove(true);
        message.setReplyMarkup(replyKeyboardRemove);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public boolean verificationNumberPhone(String numberPhone, Long chatId) throws Exception {
        boolean okNumber = true;
        Pattern pattern = Pattern.compile("^((8|\\+7)[\\- ]?)?(\\(?\\d{3}\\)?[\\- ]?)?[\\d\\- ]{7,10}$");
        Matcher matcher = pattern.matcher(numberPhone);
        if (matcher.matches()) {
            System.out.println("Правильный номер телефона");
            okNumber = true;
        } else {
            dbConnect.setUserData(chatId, "firstReaction", "global_state", statement);
            requestNumberPhone(chatId);
            System.out.println("Ошибка формата номера телефона!");
            okNumber = false;
        }
        return okNumber;
    }

    public void userOrAdmin(Long chatId) throws Exception {
        if (chatId == 743234635 || chatId == 1188351220 || chatId == 5959939548L || chatId == 5471231917L) {
            universalMethodForSend(chatId, "Главное меню!\n"
//                            + "Пользователь, "
//                            + update.getMessage().getChat().getFirstName() + ".\n"
                            + "Номер телефона: " + dbConnect.getUserPhone(chatId, statement) + "\n"
                            + "Учебное заведение: " + dbConnect.getSchool(chatId, statement) + "\n"
                            + "Класс: " + ss.spaceBetweenClassAndProf(dbConnect.getClass(chatId, statement)
                            + "\n# <u><b><i>Кнопки расположены под чатом</i></b></u> #"),
                    new String[]{"Добавить расписание", "Узнать расписание", "Узнать свое расписание", "Настройки", "Долг питания"});
        } else {
            universalMethodForSend(chatId, "Главное меню!\n"
//                            + "Пользователь, "
//                            + update.getMessage().getChat().getFirstName() + ".\n"
                            + "Номер телефона: " + dbConnect.getUserPhone(chatId, statement) + "\n"
                            + "Учебное заведение: " + dbConnect.getSchool(chatId, statement) + "\n"
                            + "Класс: " + ss.spaceBetweenClassAndProf(dbConnect.getClass(chatId, statement)
                            + "\n# <u><b><i>Кнопки расположены под чатом</i></b></u> #"),
                    new String[]{"Узнать расписание", "Узнать свое расписание", "Настройки", "Долг питания"});
        }
    }

    public void requestNumberPhone(Long chatId) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        KeyboardButton button = new KeyboardButton();
        List<KeyboardRow> buttons = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        button.setText("Отправить контакт");
        button.setRequestContact(true);
        keyboardFirstRow.add(button);
        buttons.add(keyboardFirstRow);
        keyboard.setKeyboard(buttons);
        keyboard.setResizeKeyboard(true);
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Нажмите на <u>кнопку</u>, чтобы отправить свой номер телефона\nдля регистрации\n"
                + "# <u><b><i>Кнопка расположена под чатом</i></b></u> #");
        message.setParseMode("HTML");
        message.setReplyMarkup(keyboard);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}


