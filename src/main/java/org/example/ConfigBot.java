package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigBot {
    public String[] forBotAndDB() throws IOException {
        Properties props = new Properties();
        try (
                InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            props.load(in);
        }
        String name = props.getProperty("bot.name");
        String token = props.getProperty("bot.token");
        String UserNameDB = props.getProperty("bot.userName");
        String UserPassDB = props.getProperty("bot.pass");
        String[] str = new String[]{name, token, UserNameDB, UserPassDB};
        return str;
    }
}
