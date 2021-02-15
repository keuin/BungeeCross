package com.keuin.bungeecross.message;

import com.keuin.bungeecross.message.user.MessageUser;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.util.Objects;

class ConcreteMessage implements Message {
    private final String sender;
    private final String message;

    ConcreteMessage(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public BaseComponent[] getRichTextMessage() {
        return new ComponentBuilder(message).create();
    }

    @Override
    public MessageUser getSender() {
        return MessageUser.build(sender, sender, sender);
    }

    @Override
    public boolean isJoinable() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", sender, message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConcreteMessage that = (ConcreteMessage) o;
        return Objects.equals(sender, that.sender) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, message);
    }
}