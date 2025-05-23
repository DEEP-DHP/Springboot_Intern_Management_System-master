package com.rh4.repositories;

import com.rh4.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepo extends JpaRepository<Message, Long> {

    // Fetch messages between two users (sender & receiver)
    List<Message> findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderByTimestamp(
            String senderId, String receiverId, String receiverId2, String senderId2
    );
    @Query("SELECT m FROM Message m WHERE (m.senderId = :senderId AND m.receiverId = :receiverId) OR (m.senderId = :receiverId AND m.receiverId = :senderId) ORDER BY m.timestamp ASC")
    List<Message> findBySenderIdAndReceiverId(@Param("senderId") String senderId, @Param("receiverId") String receiverId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.senderId = :senderId AND m.receiverId = :receiverId AND m.isRead = false")
    long countUnreadMessages(@Param("senderId") String senderId, @Param("receiverId") String receiverId);
}
