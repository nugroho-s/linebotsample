package net.scarycode.datebot;

import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import retrofit2.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@LineMessageHandler
public class EventHandler {
    @Value("${line.bot.channel-token}")
    String channelAccessToken;
    HashMap<String,String> quickReply = new HashMap<String, String>(){{
        put("/help","Daftar perintah yang bisa dipakai:\n" +
                "1. /join\n" +
                "2. /leave\n" +
                "3. /search");
    }};

    @Autowired
    private UserRepository userRepository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        String text = event.getMessage().getText();
        if (quickReply.containsKey(text))
            sendReply(event.getReplyToken(),quickReply.get(text));
        else if(text.contains("/join")){
            String[] sub = text.split("\\s+");
            String senderId = event.getSource().getSenderId();
            String name = getUserDisplayName(senderId);
            if ((sub.length!=2)||((!sub[1].equals("male"))&&(!sub[1].equals("female")))){
                sendReply(event.getReplyToken(),"Silakan ketik /join {male/female}");
                return;
            }
            if (joinHandler(senderId,name,(sub[1].equals("male"))?Gender.male:Gender.female))
                sendReply(event.getReplyToken(),"berhasil menambahkan "+name+" ke list dater");
            else
                sendReply(event.getReplyToken(), "terjadi kesalahan");
        } else if(text.contains("/search")){
            sendReply(event.getReplyToken(),"belum tersedia");
        }
    }

    @EventMapping
    public void handleFollowEvent(FollowEvent event) {
        sendReply(event.getReplyToken(),"Terima kasih telah follow "+getUserDisplayName(event.getSource().getUserId()));
    }

    public void sendReply(String replyToken,String text){
        TextMessage textMessage = new TextMessage(text);
        ReplyMessage replyMessage = new ReplyMessage(
                replyToken,
                textMessage
        );
        Response<BotApiResponse> response =
                null;
        try {
            response = LineMessagingServiceBuilder
                    .create(channelAccessToken)
                    .build()
                    .replyMessage(replyMessage)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info(response.code() + " " + response.message());
    }

    public String getUserDisplayName(String userId){
        Response<UserProfileResponse> response =
                null;
        try {
            response = LineMessagingServiceBuilder
                    .create(channelAccessToken)
                    .build()
                    .getProfile(userId)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response.isSuccessful()) {
            UserProfileResponse profile = response.body();
            return profile.getDisplayName();
        } else {
            return "unknown";
        }
    }

    public boolean joinHandler(String id,String name, Gender gender){
        if (userRepository.findOne(id)!=null)
            return false;
        userRepository.save(new User(id,name,gender));
        return true;
    }
}
