package com.keuin.bungeecross.intercommunicate.repeater;

import com.keuin.bungeecross.intercommunicate.message.Message;
import com.keuin.bungeecross.intercommunicate.user.PlayerUser;
import com.keuin.bungeecross.intercommunicate.user.RepeatableUser;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class InGameCommandEchoRepeater implements RepeatableUser {

    private final ProxiedPlayer proxiedPlayer;
    private final PlayerUser playerUser;

    public InGameCommandEchoRepeater(String playerId, ProxyServer proxyServer) {
        if (playerId == null)
            throw new IllegalArgumentException("playerId must not be null.");
        this.proxiedPlayer = proxyServer.getPlayer(playerId);
        if (proxiedPlayer == null)
            throw new IllegalArgumentException("Player does not exist.");
        this.playerUser = PlayerUser.fromProxiedPlayer(proxiedPlayer);
    }

    public InGameCommandEchoRepeater(UUID playerUUID, ProxyServer proxyServer) {
        if (playerUUID == null)
            throw new IllegalArgumentException("playerUUID must not be null.");
        this.proxiedPlayer = proxyServer.getPlayer(playerUUID);
        this.playerUser = PlayerUser.fromProxiedPlayer(proxiedPlayer);
    }

    public InGameCommandEchoRepeater(ProxiedPlayer proxiedPlayer) {
        this.proxiedPlayer = proxiedPlayer;
        if (proxiedPlayer == null)
            throw new IllegalArgumentException("proxiedPlayer must not be null.");
        this.playerUser = PlayerUser.fromProxiedPlayer(proxiedPlayer);
    }

    @Override
    public void repeat(Message message) {
        proxiedPlayer.sendMessage(message.getRichTextMessage());
    }

    @Override
    public String toString() {
        return String.format("Player(name=%s, uuid=%s)", this.proxiedPlayer.getName(), this.proxiedPlayer.getUniqueId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InGameCommandEchoRepeater that = (InGameCommandEchoRepeater) o;
        return playerUser.equals(that.playerUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerUser);
    }

    @Override
    public @NotNull String getName() {
        return Objects.requireNonNull(playerUser.getName());
    }

    @Override
    public UUID getUUID() {
        return playerUser.getUUID();
    }

    @Override
    public @NotNull String getId() {
        return Objects.requireNonNull(playerUser.getId());
    }

    @Override
    public String getLocation() {
        return playerUser.getLocation();
    }
}
