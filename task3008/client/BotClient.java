package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class BotClient extends Client {

    public static void main(String[] args) {
        new BotClient().run();
    }

    @Override
    protected String getUserName() {
        return "date_bot_" + (int) (Math.random() * 100);
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
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
            switch (split[1]) {
                case "дата":
                    processFurther(split[0], "d.MM.YYYY");
                    break;
                case "день":
                    processFurther(split[0], "d");
                    break;
                case "месяц":
                    processFurther(split[0], "MMMM");
                    break;
                case "год":
                    processFurther(split[0], "YYYY");
                    break;
                case "время":
                    processFurther(split[0], "H:mm:ss");
                    break;
                case "час":
                    processFurther(split[0], "H");
                    break;
                case "минуты":
                    processFurther(split[0], "m");
                    break;
                case "секунды":
                    processFurther(split[0], "s");
                    break;
            }
        }

        private void processFurther(String name, String pattern) {
            String date = new SimpleDateFormat(pattern).format(new GregorianCalendar().getTime());
            sendTextMessage(String.format("Информация для %s: %s", name, date));
        }
    }
}
