package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import static com.javarush.task.task30.task3008.client.BotClient.DateFormat.*;

public class BotClient extends Client {

    public static void main(String[] args) {
        new BotClient().run();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected String getUserName() {
        return "date_bot_" + (int) (Math.random() * 100);
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    public class BotSocketThread extends Client.SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (!message.contains(": ")) return;

            String[] split = message.split(": ");
            String former = "Информация для %s: %s";

            switch (split[1]) {
                case "дата":
                    sendTextMessage(String.format(former, split[0], DATE));
                    break;
                case "день":
                    sendTextMessage(String.format(former, split[0], DAY));
                    break;
                case "месяц":
                    sendTextMessage(String.format(former, split[0], MONTH));
                    break;
                case "год":
                    sendTextMessage(String.format(former, split[0], YEAR));
                    break;
                case "время":
                    sendTextMessage(String.format(former, split[0], TIME));
                    break;
                case "час":
                    sendTextMessage(String.format(former, split[0], HOUR));
                    break;
                case "минуты":
                    sendTextMessage(String.format(former, split[0], MINUTES));
                    break;
                case "секунды":
                    sendTextMessage(String.format(former, split[0], SECONDS));
                    break;
            }


        }
    }

    enum DateFormat {
        DATE("d.MM.YYYY"), DAY("d"), MONTH("MMMM"), YEAR("YYYY"), TIME("H:mm:ss"), HOUR("H"), MINUTES("m"), SECONDS("s");

        private final SimpleDateFormat dateFormat;

        DateFormat(String pattern) {
            dateFormat = new SimpleDateFormat(pattern);
        }

        @Override
        public String toString() {
            return dateFormat.format(new GregorianCalendar().getTime());
        }
    }
}