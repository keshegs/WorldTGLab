package io.project.worldlabs.service;

import io.project.worldlabs.config.BotConfig;
import io.project.worldlabs.model.User;
import io.project.worldlabs.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    final BotConfig config;

    static final String HELP_TEXT = "Я смотрел на свое отражение в зеркале, заглядывая в душу через глаза. Моя грязная, порочная и никчемная душа улыбалась мне в ответ. Ужасная сторона моего Я поработив меня, захватила все, и ликовала. Раздевшись и включив музыку я стоял перед собственным отражением абсолютно нагой. Без прикрас и с изъянами, вот он я. Забирай меня. Но улыбка пропала. Отражение больше не скалилось своей уродливой гримасой, ведь музыка начала звучать. Звучала, проникая в глубины, туда, где скрыто что то запредельное, освещаемое мерцанием огонька. Я смотрел на себя, понимая, что не все еще потеряно. Точнее ничего не потеряно, ведь ничего еще не начато. А начнется тогда, когда я захочу и поверю. Поверю в безграничность красоты падения. Падения в объятия. Полет к истокам. А музыка продолжала делать свое предначертанное дело. Она просто звучала и кто то нашептывал мне, что мы все непременно будем счастливы. Лишь только эта музыка звучит в такт моему сердцу, которое теперь стучит…";


    public TelegramBot(BotConfig config){
        this.config = config;

        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start","Стартуем!"));
        listofCommands.add(new BotCommand("/mydata","что ты знаешь обо мне?"));
        listofCommands.add(new BotCommand("/deletedata","снести мои данные"));
        listofCommands.add(new BotCommand("/help","рятуйте мене"));
        listofCommands.add(new BotCommand("/settings","настройки"));
        try {
            this.execute(new SetMyCommands(listofCommands,new BotCommandScopeDefault(),null));
        }catch (TelegramApiException e){
            log.error("Ошибка меню" + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

    if (update.hasMessage()  && update.getMessage().hasText()){
        String messageText = update.getMessage().getText();

        long chatId = update.getMessage().getChatId();

        switch (messageText){
            case "/start":

                    registerUser(update.getMessage());

                    startCommandRecieved(chatId, update.getMessage().getChat().getFirstName());
                    break;
            case "/help":
                sendMessage(chatId, HELP_TEXT);
                break;
            default:
                sendMessage(chatId,"Братка, я такое не умею(");

        }

    }
    }

    private void registerUser(Message msg) {

        if (userRepository.findById(msg.getChatId()).isEmpty()){

            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("Пользователь сохранен" + user);

        }

    }

    private void startCommandRecieved(long chatId, String name)  {


        String answer = "Добро пожаловать," + name + "! Я Антон, ебануто опытный, привлекательный, с сильной технической базой соискатель готов снизойти и поработать у тебя в компании за 50к в месяц!";

        log.info("Отправлено пользователю" + name);

        sendMessage(chatId,answer);
    }

    private void sendMessage(long chatId, String textToSend)  {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try{
            execute(message);
        }
        catch (TelegramApiException e){
log.error("Ошибка" + e.getMessage());
        }
    }
}
