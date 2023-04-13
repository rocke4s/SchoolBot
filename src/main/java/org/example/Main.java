package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.sql.SQLException;

public class Main {
    private static final Bot bot = new Bot();

    public static void main(String[] args) throws TelegramApiException, SQLException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);
        go(60000);
    }

    private static void go(long time) {
        Thread t = new Thread(new Runnable() { // Создаем поток
            @Override
            public void run() {
                while (true) {
                    bot.TimerTask();
                    waiting(time);
                }
            }
        });
        t.start();
    }

    private static void waiting(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }


}
