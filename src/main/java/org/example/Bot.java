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
    private final DBConnect dbConnect = new DBConnect();
    private final ShowSchedule ss = new ShowSchedule();

    public Bot() throws SQLException {
    }


    public void TimerTask() {
        Date date = new Date();
        try {
            if (date.getDay() != 0) {
                if (date.getHours() == 17 && date.getMinutes() == 16) {
                    String tomorrow = DayOfWeek.byDayOfWeek(date.getDay() + 1);
                    String tomorrowEnd = DayOfWeek.byDayOfWeekEnds(date.getDay() + 1);
                    String schoolar = "", classar = "";
                    for (int x = 0; x < dbConnect.getAllUsers().size(); x++) {
                        System.out.println(dbConnect.getSubToSchedule(Long.valueOf(dbConnect.getAllUsers().get(x))));
                        if (dbConnect.getSubToSchedule(Long.valueOf(dbConnect.getAllUsers().get(x))) != null && dbConnect.getSubToSchedule(Long.valueOf(dbConnect.getAllUsers().get(x))).equalsIgnoreCase("true")) {
                            schoolar = dbConnect.getSchool(Long.valueOf(dbConnect.getAllUsers().get(x)));
                            classar = dbConnect.getClass(Long.valueOf(dbConnect.getAllUsers().get(x)));
                            sendJustMessage(Long.valueOf(dbConnect.getAllUsers().get(x)), "Расписание на " + tomorrowEnd);
                            showSheduleEveryDay(tomorrow, schoolar, classar, Long.valueOf(dbConnect.getAllUsers().get(x)));
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

    public void rangeClass(long chatId, String range, String[] rangeVal) {
        try {
            dbConnect.setUserData(chatId, range, "user_state");
            universalMethodForSend(chatId, "Выберите номер класса:", rangeVal);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void changeStateSub(long chatId, String bool, Update update, String messegeChanges) {
        try {
            sendJustMessage(chatId, messegeChanges);
            setsUserData(chatId, bool, "subtoschedule", "default", "user_state");
            userOrAdmin(chatId, update);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setsUserData(long chatId, String selectFor, String state, String data, String typeData) {
        try {
            dbConnect.setUserData(chatId, selectFor, state);
            dbConnect.setUserData(chatId, data, typeData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPhoto(Long chatId) throws Exception {
        dbConnect.setUserData(chatId, "firstReaction", "global_state");
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId + "");
        sendPhoto.setPhoto(new InputFile("https://ibb.co/bdJg3KD"));
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        long chatId = update.getMessage().getChatId();
//        try {
//            System.out.println(dbConnect.getState(chatId) + " - " + dbConnect.getGlobalState(chatId));
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
        System.out.println(update.getMessage().getChat().getFirstName() + " - " + update.getMessage().getChat().getUserName());
        try {
            if (dbConnect.getChatId(chatId) == null) {
                dbConnect.createUser(chatId);
                dbConnect.setUserData(chatId, "firstReaction", "global_state");
                sendPhoto(chatId);
                sendJustMessage(chatId, "Добро пожаловать! Отправьте свой номер через скрепку");
            }
            System.out.println(update.getMessage().getText());
            if (dbConnect.getGlobalState(chatId).equals("firstReaction")) {
                setsUserData(chatId, "regPage", "global_state", "regPage_1", "user_state");
            }

            if (dbConnect.getGlobalState(chatId).equals("regPage")) {
                switch (dbConnect.getState(chatId)) {
                    case "regPage_1":
                        if (update.getMessage().getContact() != null &&
                                verificationNumberPhone(update.getMessage().getContact().getPhoneNumber().toString(), chatId)) {
                            setsUserData(chatId, update.getMessage().getContact().getPhoneNumber().toString(), "user_numberphone", "regPage_2", "user_state");
                            sendSchoolInCity(chatId, "Выберите школу:", 1);
                            dbConnect.setPage(chatId, 1);
                        }
                        break;
                    case "regPage_2":
                        switch (update.getMessage().getText()) {
                            case "->":
                                dbConnect.setUserData(chatId, "regPage_2", "user_state");
                                dbConnect.setPage(chatId, dbConnect.getPage(chatId) + 1);
                                sendSchoolInCity(chatId, "Выберите школу:", dbConnect.getPage(chatId));
                                break;
                            case "<-":
                                dbConnect.setUserData(chatId, "regPage_2", "user_state");
                                dbConnect.setPage(chatId, dbConnect.getPage(chatId) - 1);
                                sendSchoolInCity(chatId, "Выберите школу:", dbConnect.getPage(chatId));
                                break;
                            default:
                                for (String str : dbConnect.getAllSchool()) {
                                    if (update.getMessage().getText().equals(str)) {
                                        dbConnect.setUserData(chatId, "regPage_3", "user_state");
                                        sendClassInSchool(chatId, "Выберите класс:", 1, update.getMessage().getText());
                                        dbConnect.setUserData(chatId, update.getMessage().getText(), "user_school");
                                        dbConnect.setPage(chatId, 1);
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
                                    dbConnect.setUserData(chatId, "regPage_3", "user_state");
                                    dbConnect.setPage(chatId, dbConnect.getPage(chatId) + 1);
                                    sendClassInSchool(chatId, "Выберите класс:", dbConnect.getPage(chatId), dbConnect.getSchool(chatId));
                                    break;
                                case "<-":
                                    dbConnect.setUserData(chatId, "regPage_3", "user_state");
                                    dbConnect.setPage(chatId, dbConnect.getPage(chatId) - 1);
                                    sendClassInSchool(chatId, "Выберите класс:", dbConnect.getPage(chatId), dbConnect.getSchool(chatId));
                                    break;
                                default:
                                    for (String str : dbConnect.getAllClass(dbConnect.getSchool(chatId))) {
                                        if (update.getMessage().getText().equals(str)) {
                                            setsUserData(chatId, "Main", "global_state", update.getMessage().getText().replaceFirst(" ", ""), "user_class");
//                                            dbConnect.setUserData(chatId, "Main", "global_state");
//                                            dbConnect.setUserData(chatId, update.getMessage().getText(), "user_class");
                                            sendJustMessage(chatId, "Регистрация прошла успешно!");
                                            userOrAdmin(chatId, update);
                                            break;
                                        }
                                    }
                                    break;

                            }
                        }
                        break;
                }
            }
            if (dbConnect.getChatId(chatId) != null && dbConnect.getGlobalState(chatId).equals("Main")) {
                try {
                    if (!update.hasMessage()) {
                        System.out.println("test");
                    }
                    // if (update.hasMessage()) {
                    String messageText = update.getMessage().getText();
                    if (messageText.equals("1-4") || messageText.equals("5-8") || messageText.equals("9-11")) {
                        if (messageText.equals("1-4")) {
                            rangeClass(chatId, messageText, rangeElClas);
                        } else if (messageText.equals("5-8")) {
                            rangeClass(chatId, messageText, rangeMidClas);
                        } else if (messageText.equals("9-11")) {
                            rangeClass(chatId, messageText, rangeHigClas);
                        }
                    }

                    if (Classes.searchClass(messageText)) {
                        setsUserData(chatId, "SELECT-NUMBER", "user_state", messageText, "user_selectclass");
                        universalMethodForSend(chatId, "Выберите букву класса:", ss.getClassLetter(messageText));
                    }

                    if (SubClasses.searchSubClasses(messageText)) {
                        if (!dbConnect.getSelectClass(chatId).equals("11")) {
                            setsUserData(chatId, "SELECT-LETTER", "user_state", messageText, "user_selectwletter");
                            universalMethodForSend(chatId, "Расписание на всю неделю или на день?", new String[]{"Неделя", "День", "Вернуться"});

                        } else {
                            setsUserData(chatId, "SELECT-PROF", "user_state", messageText, "user_selectwletter");
                            universalMethodForSend(chatId, "Выберите профиль:", dbConnect.getClassProfile(chatId, dbConnect.getLetterAndNumberClass(chatId)));//todo нужно достать профили с базы
                        }
                    }

                    if (DirectionClasses.searchDirectionClasses(messageText)) {
                        setsUserData(chatId, "SELECT-LETTER", "user_state", dbConnect.getLetter(chatId) + messageText, "user_selectwletter");
                        universalMethodForSend(chatId, "Расписание на всю неделю или на день?", new String[]{"Неделя", "День", "Вернуться"});
                    }

                    if (DayOfWeek.searchDayOfWeek(messageText)) {
                        try {
                            System.out.println(messageText);
                            if (dbConnect.getState(chatId).equals("SELECT-DAY-2")) {
                                sendJustMessage(chatId, ss.showSchedule(chatId, dbConnect.getClass(chatId), update.getMessage().getText(), dbConnect.getSchool(chatId)));
                                dbConnect.setUserData(chatId, "default", "user_state");
                                userOrAdmin(chatId, update);
                            } else {
                                sendJustMessage(chatId, ss.showSchedule(chatId, dbConnect.getSelectClass(chatId) + dbConnect.getLetter(chatId), update.getMessage().getText(), dbConnect.getSchool(chatId)));
// TODO в данном случае у нас три  раза setUserData, если два раза вызвать меняя последний параметр будет работать так же?
                                dbConnect.setUserData(chatId, "default", "user_state");
                                dbConnect.setUserData(chatId, "", "user_selectclass");
                                dbConnect.setUserData(chatId, "", "user_selectwletter");
                                userOrAdmin(chatId, update);
                            }
                        } catch (SQLException e) {
                            System.out.println(e);
                        }
                    }
                    switch (messageText) {
                        case "/start":
                            userOrAdmin(chatId, update);
                            break;
                        case "Добавить расписание":
                            if (AdminShedule.searchAdmin(chatId)) {// TODO протестить, не уверен что верно поиск сделал
                                setsUserData(chatId, "addSchedulePage", "global_state", "addSchedulePage", "user_state");
                                universalMethodForSend(chatId, "Выберите школу для добавления расписания", new String[]{"Гимназия №2"});
                            }
                            break;
                        case "Узнать свое расписание":
                            try {
                                universalMethodForSend(chatId, "Расписание на всю неделю или на день?", new String[]{"Неделя", "День",});
                                dbConnect.setUserData(chatId, "vibor-svoi", "user_state");
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case "Узнать расписание":
                            try {
                                universalMethodForSend(chatId, "Выберите номер класса:", new String[]{"5-8", "9-11"});
                                dbConnect.setUserData(chatId, "vibor_1", "user_state");
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case "Настройки":
                            try {
                                universalMethodForSend(chatId, "Подписка на уведомления каждый день в 16:00", new String[]{"Подписаться на уведомления", "Убрать подписку", "Удалить аккаунт", "Вернуться"});
                                dbConnect.setUserData(chatId, "subs_schedule", "user_state");
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case "Удалить аккаунт":
                            if (dbConnect.getState(chatId).equals("subs_schedule")) {
                                sendJustMessage(chatId, "Ваш аккаунт удален!");
                                dbConnect.deleteAccount(chatId);
                            }
                        case "Подписаться на уведомления":
                            if (dbConnect.getState(chatId).equals("subs_schedule")) {
                                changeStateSub(chatId, "true", update, "Подписка оформлена!");
                            }
                            break;
                        case "Убрать подписку":
                            if (dbConnect.getState(chatId).equals("subs_schedule")) {
                                changeStateSub(chatId, "false", update, "Подписка отозвана!");
                            }
                            break;
                        case "Неделя":
                            try {
                                if (dbConnect.getState(chatId).equals("vibor-svoi")) {
                                    ss.showSchedule(dbConnect.getClass(chatId) + dbConnect.getLetter(chatId), chatId, dbConnect.getSchool(chatId));
                                    userOrAdmin(chatId, update);
                                    dbConnect.setUserData(chatId, "default", "user_state");
                                } else {
                                    ss.showSchedule(dbConnect.getSelectClass(chatId) + dbConnect.getLetter(chatId), chatId, dbConnect.getSchool(chatId));
                                    userOrAdmin(chatId, update);
//                                setsUserData(chatId, "default", "user_state", "", "user_selectclass");
//                                setsUserData(chatId, "default", "user_state", "", "user_selectwletter");
// TODO в данном случае у нас три  раза setUserData, если два раза вызвать меняя последний параметр будет работать так же?нет
                                    dbConnect.setUserData(chatId, "default", "user_state");
                                    dbConnect.setUserData(chatId, "", "user_selectclass");
                                    dbConnect.setUserData(chatId, "", "user_selectwletter");
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case "Вернуться":
                            try {
                                if (dbConnect.getState(chatId).equals("subs_schedule")) {
                                    userOrAdmin(chatId, update);
                                    dbConnect.setUserData(chatId, "default", "user_state");
                                    break;
                                }
                                if (dbConnect.getState(chatId).equals("SELECT-DAY-2")) {
                                    universalMethodForSend(chatId, "Расписание на всю неделю или на день?", new String[]{"Неделя", "День", "Вернуться"});
                                    dbConnect.setUserData(chatId, "vibor-svoi", "user_state");
                                    break;
                                }
                                if (dbConnect.getState(chatId).equals("vibor-svoi")) {
                                    userOrAdmin(chatId, update);
                                    dbConnect.setUserData(chatId, "default", "user_state");
                                    break;
                                }
                                if (dbConnect.getState(chatId).equals("SELECT-DAY")) {
                                    universalMethodForSend(chatId, "Расписание на всю неделю или на день?", new String[]{"Неделя", "День", "Вернуться"});
                                    dbConnect.setUserData(chatId, "SELECT-LETTER", "user_state");
                                    break;
                                }
                                if (dbConnect.getState(chatId).equals("SELECT-DAY")) {
                                    universalMethodForSend(chatId, "Расписание на всю неделю или на день?", new String[]{"Неделя", "День", "Вернуться"});
                                    dbConnect.setUserData(chatId, "SELECT-LETTER", "user_state");
                                    break;
                                }
                                if (dbConnect.getState(chatId).equals("SELECT-LETTER")) {
                                    universalMethodForSend(chatId, "Выберите букву класса:", ss.getClassLetter(dbConnect.getSelectClass(chatId)));
                                    setsUserData(chatId, "SELECT-NUMBER", "user_state", "", "user_selectwletter");
                                    break;
                                }
                                if (dbConnect.getState(chatId).equals("SELECT-NUMBER")) {
                                    universalMethodForSend(chatId, "Выберите номер класса:", new String[]{"5-8", "9-11"});
                                    setsUserData(chatId, "vibor_1", "user_state", "", "user_selectclass");
                                    break;
                                }
                                if (dbConnect.getState(chatId).equals("1-4") || dbConnect.getState(chatId).equals("5-8") || dbConnect.getState(chatId).equals("9-11")) {
                                    userOrAdmin(chatId, update);
                                    dbConnect.setUserData(chatId, "default", "user_state");
                                    break;
                                }

                            } catch (Exception e) {
                                System.out.println(e);
                            }
                            break;
                        case "День":
                            try {
                                if (dbConnect.getState(chatId).equals("vibor-svoi")) {
                                    dbConnect.setUserData(chatId, "SELECT-DAY-2", "user_state");
                                    universalMethodForSend(chatId, "Выберите день недели:", DOTW);
                                } else {
                                    dbConnect.setUserData(chatId, "SELECT-DAY", "user_state");
                                    universalMethodForSend(chatId, "Выберите день недели:", DOTW);
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        default:
                            try {
                                dbConnect.setUserData(chatId, "default", "user_state");
//                                    sendText(chatId, "Я не понимаю..");
                            } catch (Exception e) {
                                System.out.println(e);
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
            if (dbConnect.getGlobalState(chatId).equals("addSchedulePage")) {
                for (int i = 0; i < dbConnect.getAllSchool().size(); i++) {
                    if (dbConnect.getAllSchool().get(i).equals(update.getMessage().getText())) {
                        dbConnect.setUserData(chatId, "addSchedulePage2", "user_state");
                        universalMethodForSend(chatId, "Отправьте файл формата Excel", new String[]{"Вернуться"});
                        System.out.println(update.getMessage().getText());
                        break;
                    } else {
                        System.out.println("Ожидание выбора!");
                    }

                }
                try {
                    if (dbConnect.getState(chatId).equals("addSchedulePage2")) {
                        if (update.getMessage().hasText() && update.getMessage().getText().equals("Вернуться")) {
                            userOrAdmin(chatId, update);
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
                                    nwSchedule = db.dbAddSchedule(txt, dbConnect.getSchool(chatId));
                                } catch (SQLException | ClassNotFoundException | IOException e) {
                                    System.out.println(e);
                                    throw new RuntimeException(e);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                dbConnect.setUserData(chatId, "Main", "global_state");
                                Date date = new Date();
                                String tomorrow = DayOfWeek.byDayOfWeek(date.getDay() + 1);
                                String tomorrow2 = DayOfWeek.byDayOfWeekEnds(date.getDay() + 1);
                                for (int s = 0; s < dbConnect.getAllUsers().size(); s++) {
                                    sendJustMessage(Long.valueOf(dbConnect.getAllUsers().get(s)), "Расписание на " + tomorrow2);
                                    if (AdminShedule.searchAdmin(Long.valueOf(dbConnect.getAllUsers().get(s)))) {
                                        universalMethodForSend(Long.valueOf(dbConnect.getAllUsers().get(s)),
                                                ss.showSchedule(Long.valueOf(dbConnect.getAllUsers().get(s)),
                                                        dbConnect.getClass(Long.valueOf(dbConnect.getAllUsers().get(s))),
                                                        tomorrow, dbConnect.getSchool(Long.valueOf(dbConnect.getAllUsers().get(s)))),
                                                new String[]{"Добавить расписание", "Узнать расписание", "Узнать свое расписание", "Настройки"});
                                    } else {
                                        universalMethodForSend(Long.valueOf(dbConnect.getAllUsers().get(s)),
                                                ss.showSchedule(Long.valueOf(dbConnect.getAllUsers().get(s)),
                                                        dbConnect.getClass(Long.valueOf(dbConnect.getAllUsers().get(s))),
                                                        tomorrow, dbConnect.getSchool(Long.valueOf(dbConnect.getAllUsers().get(s)))),
                                                new String[]{"Узнать расписание", "Узнать свое расписание", "Настройки"});
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
        initReplyKeyboardMarkup(replyKeyboardMarkup);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        if (page == 1) {
            if (dbConnect.getAllSchool().size() == 3) {
                for (int i = 0; i < 3; i++) {
                    keyboardFirstRow.add(dbConnect.getAllSchool().get(i));
                }
            }
            if (dbConnect.getAllSchool().size() > 3) {
                for (int i = 0; i < 3; i++) {
                    keyboardFirstRow.add(dbConnect.getAllSchool().get(i));
                }
                keyboardSecondRow.add("->");
            }
            if (dbConnect.getAllSchool().size() == 2) {
                for (int i = 0; i < 2; i++) {
                    keyboardFirstRow.add(dbConnect.getAllSchool().get(i));
                }
            }
            if (dbConnect.getAllSchool().size() == 1) {
                for (int i = 0; i < 1; i++) {
                    keyboardFirstRow.add(dbConnect.getAllSchool().get(i));
                }
            }
        }

        if (page == 2) {
            for (int i = 3; i < 26; i++) {
                keyboardFirstRow.add(dbConnect.getAllSchool().get(i));
            }
            keyboardSecondRow.add("<-");
            keyboardSecondRow.add("->");
        }
        if (page == 3) {
            for (int i = 6; i < 9; i++) {
                keyboardFirstRow.add(dbConnect.getAllSchool().get(i));
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
            keyboardFirstRow.add(dbConnect.getAllClass(school).get(i));
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
        initReplyKeyboardMarkup(replyKeyboardMarkup);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        KeyboardRow keyboardThreeRow = new KeyboardRow();
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

    public String spaceBetweenClassAndProf(String stroka) {
        String strClass = stroka.substring(0, 2);
        if (strClass.equals("11")) {
            strClass = stroka.substring(0, 3);
            String strProf = stroka.substring(3);
            stroka = strClass + " " + strProf;
        }
        return stroka;
    }

    public void uploadFile(Update update, long chatId) throws Exception {
        if (update.getMessage().getDocument() == null) {
            System.out.println("файл не отправлен!");
        } else {
            var file_name = update.getMessage().getDocument().getFileName();
            System.out.println(file_name);
            StringUtils.right(file_name, 4);
            if (StringUtils.right(file_name, 4).equals(".xml") || StringUtils.right(file_name, 5).equals(".xlsx")) {
                var file_id = update.getMessage().getDocument().getFileId();
                URL url = new URL("https://api.telegram.org/bot" + "6094737832:AAFvZf3Jsh9aBXN9tJPnyNX7S5lUSq_ru5c" + "/getFile?file_id=" + file_id);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String res = in.readLine();
                JSONObject jresult = new JSONObject(res);
                JSONObject path = jresult.getJSONObject("result");
                String file_path = path.getString("file_path");
                System.out.println("Start upload");
                File localFile = new File(file_name);
                txt = file_name;
                InputStream is = new URL("https://api.telegram.org/file/bot" + "6094737832:AAFvZf3Jsh9aBXN9tJPnyNX7S5lUSq_ru5c" + "/" + file_path).openStream();
                FileUtils.copyInputStreamToFile(is, localFile);
                in.close();
                is.close();
                System.out.println("Uploaded!");
            } else {
                universalMethodForSend(chatId, "Нужен файл в формате эксель",
                        new String[]{"Узнать расписание", "Узнать свое расписание", "Настройки"});
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
        sendJustMessage(chatId, ss.showSchedule(chatId, classar, tomorrow, schoolar));
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
                    for (int x = 0; x < dbConnect.getUserNewScheduleInSchool(dbConnect.getSchool(chatId), newSchedule.get(w).substring(start, end)).size(); x++) {
                        message.setChatId(dbConnect.getUserNewScheduleInSchool(dbConnect.getSchool(chatId), newSchedule.get(w).substring(start, end)).get(x));
                        if (stroka.contains(dbConnect.getClass(Long.valueOf(message.getChatId())))) {
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
            dbConnect.setUserData(chatId, "firstReaction", "global_state");
            sendPhoto(chatId);
            sendJustMessage(chatId, "Добро пожаловать! Отправьте свой номер через скрепку");
            System.out.println("Ошибка формата номера телефона!");
            okNumber = false;
        }
        return okNumber;
    }

    public void userOrAdmin(Long chatId, Update update) throws Exception {
        if (chatId == 743234635 || chatId == 1188351220 || chatId == 5959939548L || chatId == 5471231917L) {
            universalMethodForSend(chatId, "Главное меню!\n" + "Пользователь, "
                            + update.getMessage().getChat().getFirstName() + ".\n"
                            + "Номер телефона: " + dbConnect.getUserPhone(chatId) + "\n"
                            + "Учебное заведение: " + dbConnect.getSchool(chatId) + "\n"
                            + "Класс: " + dbConnect.getClass(chatId),
                    new String[]{"Добавить расписание", "Узнать расписание", "Узнать свое расписание", "Настройки"});
        } else {
            universalMethodForSend(chatId, "Главное меню!\n" + "Пользователь, "
                            + update.getMessage().getChat().getFirstName() + ".\n"
                            + "Номер телефона: " + dbConnect.getUserPhone(chatId) + "\n"
                            + "Учебное заведение: " + dbConnect.getSchool(chatId) + "\n"
                            + "Класс: " + dbConnect.getClass(chatId),
                    new String[]{"Узнать расписание", "Узнать свое расписание", "Настройки"});
        }
    }
}


