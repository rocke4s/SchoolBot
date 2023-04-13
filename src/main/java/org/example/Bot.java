package org.example;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.*;
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bot extends TelegramLongPollingBot {
    String txt = "";
    String[] DOTW = new String[]{"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота"};
    String dotatw = "";
    String school = "";
    List<String> nwSchedule = new ArrayList<>();
    private DBConnect dbConnect = new DBConnect();
    private ShowSchedule ss = new ShowSchedule();

    public void TimerTask() {
        Date date = new Date();
        try {
            if (date.getDay() != 0) {
                if (date.getHours() == 16 && date.getMinutes() == 0) {
                    String dayNow = "";
                    switch (date.getDay()) {
                        case 1:
                            dayNow = "Вторник";
                            break;
                        case 2:
                            dayNow = "Среда";
                            break;
                        case 3:
                            dayNow = "Четверг";
                            break;
                        case 4:
                            dayNow = "Пятница";
                            break;
                        case 5:
                            dayNow = "Суббота";
                            break;
                        case 6:
                            dayNow = "Понедельник";
                            break;
                        default:
                            break;

                    }
                    String schoolar = "", classar = "";
                    for (int x = 0; x < dbConnect.getAllUsers().size(); x++) {
                        System.out.println(dbConnect.getSubToSchedule(Long.valueOf(dbConnect.getAllUsers().get(x))));
                        if (dbConnect.getSubToSchedule(Long.valueOf(dbConnect.getAllUsers().get(x))) != null && dbConnect.getSubToSchedule(Long.valueOf(dbConnect.getAllUsers().get(x))).equalsIgnoreCase("true")) {
                            schoolar = dbConnect.getSchool(Long.valueOf(dbConnect.getAllUsers().get(x)));
                            classar = dbConnect.getClass(Long.valueOf(dbConnect.getAllUsers().get(x)));
                            sendJustMessage(Long.valueOf(dbConnect.getAllUsers().get(x)), "Расписание на " + dayNow);
                            showSheduleEveryDay(dayNow, schoolar, classar, Long.valueOf(dbConnect.getAllUsers().get(x)));
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
        return "Gim2bot";
    }

    @Override
    public String getBotToken() {
        return "6094737832:AAFvZf3Jsh9aBXN9tJPnyNX7S5lUSq_ru5c";
    }

    @Override
    public void onUpdateReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        System.out.println(update.getMessage().getChat().getFirstName() + " - " + update.getMessage().getChat().getUserName());
        try {
            if (dbConnect.getChatId(chatId) == null) {
                dbConnect.createUser(chatId);
                dbConnect.setUserData(chatId, "firstReaction", "global_state");
            }
            System.out.println(update.getMessage().getText());
            if (dbConnect.getGlobalState(chatId).equals("firstReaction")) {
                dbConnect.setUserData(chatId, "regPage", "global_state");
                dbConnect.setUserData(chatId, "regPage_1", "user_state");
            }

            if (dbConnect.getGlobalState(chatId).equals("regPage")) {
                switch (dbConnect.getState(chatId)) {
                    case "regPage_1":
                        if (verificationNumberPhone(update.getMessage().getText(), chatId)) {
                            dbConnect.setUserData(chatId, update.getMessage().getText(), "user_numberphone");
                            dbConnect.setUserData(chatId, "regPage_2", "user_state");
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
                                            dbConnect.setUserData(chatId, "Main", "global_state");
                                            dbConnect.setUserData(chatId, update.getMessage().getText(), "user_class");
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
                    if (update.hasMessage()) {
                        String messageText = update.getMessage().getText();
                        switch (messageText) {
                            case "/start":
                                userOrAdmin(chatId, update);
                                break;
                            case "Добавить расписание":
                                try {
                                    if (chatId == 743234635 || chatId == 1188351220 || chatId == 5959939548L || chatId == 5471231917L) {
                                        dbConnect.setUserData(chatId, "addSchedulePage", "global_state");
                                        dbConnect.setUserData(chatId, "addSchedulePage", "user_state");
                                        sendQuestionASchool(chatId, "Выберите школу для добавления расписания");
                                    }
                                } catch (Exception e) {
                                    System.out.println(e);
                                }
                                break;
                            case "Узнать свое расписание":
                                try {
                                    sendQuestion(chatId, "Расписание на всю неделю или на день?");
                                    dbConnect.setUserData(chatId, "vibor-svoi", "user_state");
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                break;
                            case "Узнать расписание":
                                try {
                                    sendClassNumber(chatId, "Выберите номер класса:");
                                    dbConnect.setUserData(chatId, "vibor_1", "user_state");
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                break;
                            case "Настройки":
                                try {
                                    sendSubToSchedule(chatId, "Подписка на уведомления каждый день в 16:00");
                                    dbConnect.setUserData(chatId, "subs_schedule", "user_state");
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                break;
                            case "Подписаться на уведомления":
                                if (dbConnect.getState(chatId).equals("subs_schedule")) {
                                    dbConnect.setUserData(chatId, "true", "subtoschedule");
                                    dbConnect.setUserData(chatId, "default", "user_state");
                                    userOrAdmin(chatId, update);
                                }
                                break;
                            case "Убрать подписку":
                                if (dbConnect.getState(chatId).equals("subs_schedule")) {
                                    dbConnect.setUserData(chatId, "false", "subtoschedule");
                                    dbConnect.setUserData(chatId, "default", "user_state");
                                    userOrAdmin(chatId, update);
                                }
                                break;
                            case "1-4":
                                try {
                                    dbConnect.setUserData(chatId, "1-4", "user_state");
                                    sendNumbOneTo(chatId, "Выберите номер класса:");
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                break;
                            case "5-8":
                                try {
                                    dbConnect.setUserData(chatId, "5-8", "user_state");
                                    sendNumbFiveTo(chatId, "Выберите номер класса:");
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                break;
                            case "9-11":
                                try {
                                    dbConnect.setUserData(chatId, "9-11", "user_state");
                                    sendNumbNineTo(chatId, "Выберите номер класса:");
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                break;
                            case "1":
                            case "2":
                            case "3":
                            case "4":
                            case "5":
                            case "6":
                            case "7":
                            case "8":
                            case "9":
                            case "10":
                            case "11":
                                try {
                                    dbConnect.setUserData(chatId, "SELECT-NUMBER", "user_state");
                                    dbConnect.setUserData(chatId, messageText, "user_selectclass");
                                    sendWord(chatId, "Выберите букву класса:", ss.getClassLetter(messageText));
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                break;
                            case "А":
                            case "Б":
                            case "В":
                            case "Г":
                            case "Д":
                                try {
                                    if (!dbConnect.getSelectClass(chatId).equals("11")) {
                                        dbConnect.setUserData(chatId, "SELECT-LETTER", "user_state");
                                        dbConnect.setUserData(chatId, messageText, "user_selectwletter");
                                        sendQuestion(chatId, "Расписание на всю неделю или на день?");

                                    } else {
                                        try {
                                            dbConnect.setUserData(chatId, "SELECT-PROF", "user_state");
                                            dbConnect.setUserData(chatId, messageText, "user_selectwletter");
                                            sendClassProfile(chatId, "Выберите профиль:");
                                        } catch (Exception e) {
                                            System.out.println(e);
                                        }

                                    }
                                } catch (Exception e) {
                                    System.out.println(e);
                                }

                                break;
                            case "гуманитарный профиль":
                            case "естественно-научный профиль":
                            case "технологческий профиль":
                                try {
                                    if (dbConnect.getState(chatId).equals("SELECT-PROF")) {
                                        dbConnect.setUserData(chatId, "SELECT-LETTER", "user_state");
                                        dbConnect.setUserData(chatId, dbConnect.getLetter(chatId) + messageText, "user_selectwletter");
                                        sendQuestion(chatId, "Расписание на всю неделю или на день?");
                                    }
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }

                                break;
                            case "Неделя":
                                try {
                                    if (dbConnect.getState(chatId).equals("vibor-svoi")) {
                                        ss.showSchedule(dbConnect.getClass(chatId) + dbConnect.getLetter(chatId), chatId, dbConnect.getSchool(chatId));
                                        userOrAdmin(chatId, update);
                                        dbConnect.setUserData(chatId, "default", "user_state");
                                    } else {
                                        DeleteMessage deleteMessage = new DeleteMessage();
                                        deleteMessage.setChatId(chatId + "");
                                        deleteMessage.setMessageId(update.getMessage().getMessageId());
                                        execute(deleteMessage);
                                        ss.showSchedule(dbConnect.getSelectClass(chatId) + dbConnect.getLetter(chatId), chatId, dbConnect.getSchool(chatId));
                                        userOrAdmin(chatId, update);
                                        dbConnect.setUserData(chatId, "default", "user_state");
                                        dbConnect.setUserData(chatId, "", "user_selectclass");
                                        dbConnect.setUserData(chatId, "", "user_selectwletter");
                                    }
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                dotatw = "";
                                break;
                            case "Вернуться":
                                try {
                                    if (dbConnect.getState(chatId).equals("subs_schedule")) {
                                        userOrAdmin(chatId, update);
                                        dbConnect.setUserData(chatId, "default", "user_state");
                                        break;
                                    }
                                    if (dbConnect.getState(chatId).equals("SELECT-DAY-2")) {
                                        sendQuestion(chatId, "Расписание на всю неделю или на день?");
                                        dbConnect.setUserData(chatId, "vibor-svoi", "user_state");
                                        break;
                                    }
                                    if (dbConnect.getState(chatId).equals("vibor-svoi")) {
                                        userOrAdmin(chatId, update);
                                        dbConnect.setUserData(chatId, "default", "user_state");
                                        break;
                                    }
                                    if (dbConnect.getState(chatId).equals("SELECT-DAY")) {
                                        sendQuestion(chatId, "Расписание на всю неделю или на день?");
                                        dbConnect.setUserData(chatId, "SELECT-LETTER", "user_state");
                                        break;
                                    }
                                    if (dbConnect.getState(chatId).equals("SELECT-DAY")) {
                                        sendQuestion(chatId, "Расписание на всю неделю или на день?");
                                        dbConnect.setUserData(chatId, "SELECT-LETTER", "user_state");
                                        break;
                                    }
                                    if (dbConnect.getState(chatId).equals("SELECT-LETTER")) {
                                        sendWord(chatId, "Выберите букву класса:", ss.getClassLetter(dbConnect.getSelectClass(chatId)));
                                        dbConnect.setUserData(chatId, "SELECT-NUMBER", "user_state");
                                        dbConnect.setUserData(chatId, "", "user_selectwletter");
                                        break;
                                    }
                                    if (dbConnect.getState(chatId).equals("SELECT-NUMBER")) {
                                        sendClassNumber(chatId, "Выберите номер класса:");
                                        dbConnect.setUserData(chatId, "vibor_1", "user_state");
                                        dbConnect.setUserData(chatId, "", "user_selectclass");
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
                                        sendDOTW(chatId, "Выберите день недели:");
                                    } else {
                                        dbConnect.setUserData(chatId, "SELECT-DAY", "user_state");
                                        sendDOTW(chatId, "Выберите день недели:");
                                    }
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                break;
                            case "Понедельник":
                            case "Вторник":
                            case "Среда":
                            case "Четверг":
                            case "Пятница":
                            case "Суббота":
                                try {
                                    System.out.println(messageText);
                                    if (dbConnect.getState(chatId).equals("SELECT-DAY-2")) {
                                        sendJustMessage(chatId, ss.showSchedule(chatId, dbConnect.getClass(chatId), update.getMessage().getText(), dbConnect.getSchool(chatId)));
                                        dbConnect.setUserData(chatId, "default", "user_state");
                                        userOrAdmin(chatId, update);
                                    } else {
                                        sendJustMessage(chatId, ss.showSchedule(chatId, dbConnect.getSelectClass(chatId) + dbConnect.getLetter(chatId), update.getMessage().getText(), dbConnect.getSchool(chatId)));
                                        dbConnect.setUserData(chatId, "default", "user_state");
                                        dbConnect.setUserData(chatId, "", "user_selectclass");
                                        dbConnect.setUserData(chatId, "", "user_selectwletter");
                                        userOrAdmin(chatId, update);
                                    }
                                } catch (SQLException e) {
                                    System.out.println(e);
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
                        school = update.getMessage().getText();
                        dbConnect.setUserData(chatId, "addSchedulePage2", "user_state");
                        sendQuestionAddSchedule(chatId, "Отправьте файл формата Excel");
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
                            dbConnect.setUserData(chatId, "default", "user_state");
                            dbConnect.setUserData(chatId, "Main", "global_state");
                        } else {
                            try {
                                uploadFile(update, chatId);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            System.out.println("txt= " + txt);
                            if (txt != "") {
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
                                String dayNow = "";
                                String dayNow2 = "";
                                switch (date.getDay()) {
                                    case 1:
                                        dayNow = "Вторник";
                                        dayNow2 = "Вторник";
                                        break;
                                    case 2:
                                        dayNow = "Среда";
                                        dayNow2 = "Среду";
                                        break;
                                    case 3:
                                        dayNow = "Четверг";
                                        dayNow2 = "Четверг";
                                        break;
                                    case 4:
                                        dayNow = "Пятница";
                                        dayNow2 = "Пятницу";
                                        break;
                                    case 5:
                                        dayNow = "Суббота";
                                        dayNow2 = "Субботу";
                                        break;
                                    case 6:
                                        dayNow = "Понедельник";
                                        dayNow2 = "Понедельник";
                                        break;
                                    default:
                                        break;

                                }
                                for (int s = 0; s < dbConnect.getAllUsers().size(); s++) {
                                    sendJustMessage(Long.valueOf(dbConnect.getAllUsers().get(s)), "Расписание на " + dayNow2);
                                    if (Long.valueOf(dbConnect.getAllUsers().get(s)) == 743234635 || Long.valueOf(dbConnect.getAllUsers().get(s)) == 1188351220 || Long.valueOf(dbConnect.getAllUsers().get(s)) == 5959939548L || Long.valueOf(dbConnect.getAllUsers().get(s)) == 5471231917L) {
                                        sendTextToAdmin(Long.valueOf(dbConnect.getAllUsers().get(s)), ss.showSchedule(Long.valueOf(dbConnect.getAllUsers().get(s)), dbConnect.getClass(Long.valueOf(dbConnect.getAllUsers().get(s))), dayNow, dbConnect.getSchool(Long.valueOf(dbConnect.getAllUsers().get(s)))));
                                    } else {
                                        sendText(Long.valueOf(dbConnect.getAllUsers().get(s)), ss.showSchedule(Long.valueOf(dbConnect.getAllUsers().get(s)), dbConnect.getClass(Long.valueOf(dbConnect.getAllUsers().get(s))), dayNow, dbConnect.getSchool(Long.valueOf(dbConnect.getAllUsers().get(s)))));
                                    }
                                }
                                // sendingNewSchedule(chatId, nwSchedule);
                                txt = "";
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

    public void sendQuestionASchool(Long who, String what) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("Гимназия №2");
//        keyboardFirstRow.add("Средняя школа №45");
//        keyboardFirstRow.add("Средняя школа №10");
//        keyboardFirstRow.add("Средняя школа №26");
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        message.enableMarkdown(true);
        message.setChatId(who.toString());
        message.setText(what);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendQuestionAddSchedule(Long who, String what) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("Вернуться");
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        message.enableMarkdown(true);
        message.setChatId(who.toString());
        message.setText(what);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendTextToAdmin(Long who, String what) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardFirstRow.add("Добавить расписание");
        keyboardFirstRow.add("Узнать расписание");
        keyboardFirstRow.add("Узнать свое расписание");
        keyboardSecondRow.add("Настройки");
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        message.enableMarkdown(true);
        message.setChatId(who.toString());
        message.setText(what);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendQuestion(Long who, String what) throws Exception {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("Неделя");
        keyboardFirstRow.add("День");
        keyboardFirstRow.add("Вернуться");
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        message.enableMarkdown(true);
        message.setChatId(who.toString());
        message.setText(what);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendNumbOneTo(Long who, String what) throws Exception {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("1");
        keyboardFirstRow.add("2");
        keyboardFirstRow.add("3");
        keyboardFirstRow.add("4");
        keyboardFirstRow.add("Вернуться");
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        message.enableMarkdown(true);
        message.setChatId(who.toString());
        message.setText(what);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendSubToSchedule(Long who, String what) throws Exception {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("Подписаться на уведомления");
        keyboardFirstRow.add("Убрать подписку");
        keyboardFirstRow.add("Вернуться");
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        message.enableMarkdown(true);
        message.setChatId(who.toString());
        message.setText(what);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendDOTW(Long who, String what) throws Exception {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        KeyboardRow keyboardThreeRow = new KeyboardRow();
        keyboardFirstRow.add("Понедельник");
        keyboardFirstRow.add("Вторник");
        keyboardFirstRow.add("Среда");
        keyboardSecondRow.add("Четверг");
        keyboardSecondRow.add("Пятница");
        keyboardSecondRow.add("Суббота");
        keyboardThreeRow.add("Вернуться");
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        keyboard.add(keyboardThreeRow);

        replyKeyboardMarkup.setKeyboard(keyboard);
        message.enableMarkdown(true);
        message.setChatId(who.toString());
        message.setText(what);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendSchoolInCity(Long who, String what, int page) throws Exception {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
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
        message.enableMarkdown(true);
        message.setChatId(who.toString());
        message.setText(what);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendClassInSchool(Long who, String what, int page, String school) throws Exception {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
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
        message.enableMarkdown(true);
        message.setChatId(who.toString());
        message.setText(what);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendNumbFiveTo(Long who, String what) throws Exception {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("5");
        keyboardFirstRow.add("6");
        keyboardFirstRow.add("7");
        keyboardFirstRow.add("8");
        keyboardFirstRow.add("Вернуться");
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        message.enableMarkdown(true);
        message.setChatId(who.toString());
        message.setText(what);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendNumbNineTo(Long who, String what) throws Exception {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("9");
        keyboardFirstRow.add("10");
        keyboardFirstRow.add("11");
        keyboardFirstRow.add("Вернуться");
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        message.enableMarkdown(true);
        message.setChatId(who.toString());
        message.setText(what);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendClassNumber(Long who, String what) throws Exception {
        ShowSchedule ss = new ShowSchedule();
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        //     int numb = 0;
        //   numb = ss.getClassNumb(dbConnect.getSchool(who));
//        if (numb < 4) {
//            keyboardFirstRow.add("1-4");
//        }
//        if (numb < 8) {
        keyboardFirstRow.add("5-8");
        //    }
        //      if (numb < 11) {
        keyboardFirstRow.add("9-11");
        //    }
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        message.enableMarkdown(true);
        message.setChatId(who.toString());
        message.setText(what);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendWord(Long who, String what, String let[]) throws Exception {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        int x = 0;
        for (String str : let) {
            if (str != null && str != "") {
                if (x < 3) {
                    keyboardFirstRow.add(str);
                } else {
                    keyboardSecondRow.add(str);
                }
                x++;
            }
        }
        keyboardSecondRow.add("Вернуться");
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        message.enableMarkdown(true);
        message.setChatId(who.toString());
        message.setText(what);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendText(Long who, String what) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardFirstRow.add("Узнать расписание");
        keyboardFirstRow.add("Узнать свое расписание");
        keyboardSecondRow.add("Настройки");
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        message.enableMarkdown(true);
        message.setChatId(who.toString());
        message.setText(what);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendClassProfile(Long who, String what) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("гуманитарный профиль");
        keyboardFirstRow.add("естественно-научный профиль");
        keyboardFirstRow.add("технологческий профиль");
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        message.enableMarkdown(true);
        message.setChatId(who.toString());
        message.setText(what);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
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
                File localFile = new File("src/main/resources/files/" + file_name);
                txt = "src/main/resources/files/" + file_name;
                InputStream is = new URL("https://api.telegram.org/file/bot" + "6094737832:AAFvZf3Jsh9aBXN9tJPnyNX7S5lUSq_ru5c" + "/" + file_path).openStream();
                FileUtils.copyInputStreamToFile(is, localFile);
                in.close();
                is.close();
                System.out.println("Uploaded!");
            } else {
                sendText(chatId, "Нужен файл в формате эксель");
            }
        }
    }

    public static void readFromExcel(String file) throws IOException {
        XSSFWorkbook myExcelBook = new XSSFWorkbook(file);
        XSSFSheet myExcelSheet = myExcelBook.getSheetAt(0);
        XSSFRow row = myExcelSheet.getRow(8);
        String name = row.getCell(2).getStringCellValue();

        myExcelBook.close();

    }

    public void showSheduleEveryDay(String dayNow, String schoolar, String classar, Long chatId) throws Exception {
        sendJustMessage(chatId, ss.showSchedule(chatId, classar, dayNow, schoolar));
    }

    public void sendingNewSchedule(Long chatId, List<String> newSchedule) throws Exception {
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

    public void sendJustMessage(Long chatId, String msg) throws Exception {
        SendMessage message = new SendMessage();
        message.enableMarkdown(true);
        message.setChatId(chatId.toString());
        message.setText(msg);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public boolean verificationNumberPhone(String numberPhone, Long chatId) throws Exception {
        boolean okNumber = true;
        Pattern pattern = Pattern.compile("\\d{11}");
        Matcher matcher = pattern.matcher(numberPhone);
        if (matcher.matches()) {
            System.out.println("Правильный номер телефона");
            okNumber = true;
        } else {
            dbConnect.setUserData(chatId, "firstReaction", "global_state");
            sendJustMessage(chatId, "Добро пожаловать! Введите номер телефона для регистрации!(прим. 89993332211");
            System.out.println("Ошибка формата номера телефона!");
            okNumber = false;
        }
        return okNumber;
    }

    public void userOrAdmin(Long chatId, Update update) throws Exception {
        if (chatId == 743234635 || chatId == 1188351220 || chatId == 5959939548L || chatId == 5471231917L) {
            sendTextToAdmin(chatId, "Главное меню!\n" + "Пользователь, "
                    + update.getMessage().getChat().getFirstName() + ".\n"
                    + "Номер телефона: " + dbConnect.getUserPhone(chatId) + "\n"
                    + "Учебное заведение: " + dbConnect.getSchool(chatId) + "\n"
                    + "Класс: " + dbConnect.getClass(chatId));
        } else {
            sendText(chatId, "Главное меню!\n" + "Пользователь, "
                    + update.getMessage().getChat().getFirstName() + ".\n"
                    + "Номер телефона: " + dbConnect.getUserPhone(chatId) + "\n"
                    + "Учебное заведение: " + dbConnect.getSchool(chatId) + "\n"
                    + "Класс: " + dbConnect.getClass(chatId));
        }
    }


}


