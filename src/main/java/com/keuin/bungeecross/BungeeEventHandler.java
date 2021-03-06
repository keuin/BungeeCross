package com.keuin.bungeecross;

import com.keuin.bungeecross.intercommunicate.message.HistoryMessage;
import com.keuin.bungeecross.intercommunicate.message.InGameMessage;
import com.keuin.bungeecross.intercommunicate.msghandler.InGameChatHandler;
import com.keuin.bungeecross.intercommunicate.user.MessageUser;
import com.keuin.bungeecross.intercommunicate.user.PlayerUser;
import com.keuin.bungeecross.mininstruction.executor.history.InGamePlayer;
import com.keuin.bungeecross.mininstruction.history.ActivityProvider;
import com.keuin.bungeecross.recentmsg.RecentMessageManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class BungeeEventHandler implements Listener {

    private final InGameChatHandler inGameChatProcessor;
    private final Plugin plugin;
    private final Logger logger = Logger.getLogger(BungeeEventHandler.class.getName());
    private final ActivityProvider activityProvider;
    private final PlayerStateChangeNotification playerStateChangeNotification;
    private final RecentMessageManager recentMessageManager;
    private final Set<UUID> connectedPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>()); // all players connected to the proxy

    private final Map<UUID, ServerInfo> joiningServers = new HashMap<>();
    private final Map<UUID, ServerInfo> serverPlayerLastJoined = new HashMap<>(); // the server players last connected to

    public BungeeEventHandler(Plugin plugin, InGameChatHandler inGameChatProcessor, ActivityProvider activityProvider, RecentMessageManager recentMessageManager) {
        this.plugin = Objects.requireNonNull(plugin);
        this.inGameChatProcessor = Objects.requireNonNull(inGameChatProcessor);
        this.activityProvider = Objects.requireNonNull(activityProvider);
        this.recentMessageManager = Objects.requireNonNull(recentMessageManager);
        this.playerStateChangeNotification = new PlayerStateChangeNotification(plugin.getProxy());
    }

    @EventHandler
    public void onServerDisconnect(ServerDisconnectEvent event) {
        try {
            activityProvider.logPlayerActivity(InGamePlayer.fromProxiedPlayer(event.getPlayer()));
        } catch (Exception e) {
            logger.warning("Failed to log player logout activity: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        try {
            ProxiedPlayer player = event.getPlayer();
            Optional.ofNullable(player).ifPresent(p -> {
                logger.info("Player " + p.getName() + " disconnected. Remove him from local online list.");
                connectedPlayers.remove(p.getUniqueId());
            });
            ServerInfo server = (player != null) ? serverPlayerLastJoined.get(player.getUniqueId()) : null;
            if (server != null)
                playerStateChangeNotification.notifyPlayerDisconnectServer(player, server);
            else
                logger.warning("Cannot get player's server while broadcasting disconnect messages.");
        } catch (Exception e) {
            logger.warning(String.format("An exception caught while handling player disconnect event: %s.", e));
        }
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if (player == null)
            return;
        ServerInfo server = event.getTarget();
        joiningServers.put(player.getUniqueId(), server);
    }

    @EventHandler
    public void onPlayerJoined(ServerConnectedEvent event) {

        // after a player joined
        ProxiedPlayer player = event.getPlayer();
        if (player == null)
            return;
        ProxyServer proxy = plugin.getProxy();
        ServerInfo server = joiningServers.get(player.getUniqueId());

        if (!joiningServers.containsKey(event.getPlayer().getUniqueId())) {
            logger.warning(String.format("Unexpected player %s. Login broadcast will not be sent.", event.getPlayer().getName()));
            return;
        }

        // log activity
        activityProvider.logPlayerActivity(InGamePlayer.fromProxiedPlayer(player));

        new Thread(() -> {
            try {
                Thread.sleep(250); // to make the message sequence correct: a stupid work-around.
            } catch (InterruptedException ignored) {
            }
            playerStateChangeNotification.notifyPlayerJoinServer(player, server);
        }).start();

        joiningServers.remove(event.getPlayer().getUniqueId());
        serverPlayerLastJoined.put(event.getPlayer().getUniqueId(), server);

        // send recent history messages
        new Thread(() -> { // The BungeeCord provides a very tedious event API, so we have to work around like this.
            try {          // Otherwise, the client won't receive any message.
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            if (connectedPlayers.add(player.getUniqueId())) {
                logger.info("Player " + player.getName() + " logged in." +
                        " Add him to local login list and send him recent messages.");
                Collection<HistoryMessage> messages = recentMessageManager.getRecentMessages();
                logger.info("Got " + messages.size() + " recent messages. Send them to the player.");
                for (HistoryMessage message : messages) {
//                    logger.info("Repeat previous message " + message.getMessage());
                    player.sendMessage(message.getRichTextMessage());
                }
            }
        }).start();
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        Connection senderConn = event.getSender();
        if (!(senderConn instanceof ProxiedPlayer)) {
            logger.severe(String.format("Sender is not a ProxiedPlayer instance: %s", event.getSender().toString()));
            return;
        }

        ProxiedPlayer sender = (ProxiedPlayer) senderConn;
        String message = event.getMessage();
        MessageUser messageUser = PlayerUser.fromProxiedPlayer(sender);

        if (message.startsWith("/"))
            return; // Do not repeat commands

        logger.info(String.format("Chat message: %s, sender: %s", message, messageUser));
        inGameChatProcessor.issue(new InGameMessage(message, sender));
    }

}