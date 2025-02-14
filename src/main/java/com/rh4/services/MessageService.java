package com.rh4.services;

import com.rh4.entities.Message;
import com.rh4.repositories.MessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepo messageRepo;

    // Save message to database
    public Message sendMessage(String senderId, String receiverId, String messageText) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setMessageText(messageText);
        return messageRepo.save(message);
    }

    // Fetch chat history between two users
    public List<Message> getChatHistory(String senderId, String receiverId) {
        return messageRepo.findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderByTimestamp(
                senderId, receiverId, receiverId, senderId);
    }
    public List<Message> getChatHistoryForBothUsers(String senderId, String receiverId) {
        return messageRepo.findBySenderIdAndReceiverId(senderId, receiverId);
    }
}