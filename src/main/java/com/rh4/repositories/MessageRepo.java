package com.rh4.repositories;

import com.rh4.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepo extends JpaRepository<Message, Long> {

    // Fetch messages between two users (sender & receiver)
    List<Message> findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderByTimestamp(
            String senderId, String receiverId, String receiverId2, String senderId2
    );
}
