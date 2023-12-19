package com.ostreach.entities.model;

import com.ostreach.entities.enums.ChatStatus;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ChatMessage {
    private String senderName;
    private String receiverName;
    private String message;
    private String date;
    private ChatStatus chatStatus;
}
