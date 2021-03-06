package com.keuin.bungeecross.mininstruction.executor;

import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.keuin.bungeecross.mininstruction.context.UserContext;
import com.keuin.bungeecross.util.PrettyComponents;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;

public final class ListExecutor extends AbstractInstructionExecutor {

    public ListExecutor() {
        super("list", "show online players in all servers.", new String[0]);
    }

    private ExecutionResult printWithOldStyle(Collection<ProxiedPlayer> players, ProxyServer proxy, MessageRepeatable echoRepeater) {
        Objects.requireNonNull(players);
        if (players.isEmpty())
            return ExecutionResult.SUCCESS;
        // players
        List<BaseComponent> echoComponents = new ArrayList<>();
        proxy.getPlayers().forEach(p -> Optional.ofNullable(p).ifPresent(
                player -> echoComponents.addAll(Arrays.asList(getPlayerPrettyComponent(player)))
        ));
        if (!echoComponents.isEmpty())
            echo(echoRepeater, echoComponents.toArray(new BaseComponent[0]));
        return ExecutionResult.SUCCESS;
    }

    private ExecutionResult printWithGroupedStyle(Collection<ProxiedPlayer> players, ProxyServer proxy, MessageRepeatable echoRepeater) {
        Objects.requireNonNull(players);
        if (players.isEmpty())
            return ExecutionResult.SUCCESS;

        // group players by server
        final SortedMap<String, Collection<ProxiedPlayer>> playerMap = new TreeMap<>();
        for (ProxiedPlayer player : players) {
            final String serverName = player.getServer().getInfo().getName();
            if (!playerMap.containsKey(serverName))
                playerMap.put(serverName, new ArrayList<>(10));
            playerMap.get(serverName).add(player);
        }

        // print all servers' players in natural order
        List<BaseComponent> echoComponents = new ArrayList<>(20);
        boolean[] isFirst = {true};
        playerMap.forEach((server, players2) -> {
            if (!isFirst[0])
                echoComponents.add(new TextComponent("\n\n"));
            isFirst[0] = false;
            echoComponents.add(PrettyComponents.createNavigableServerButton(server));
            echoComponents.add(new TextComponent("\n"));
            boolean first = true;
            for (ProxiedPlayer player : players2) {
                final BaseComponent line
                        = new TextComponent((first ? "" : " ") + player.getName());
                line.setColor(ChatColor.WHITE);
                echoComponents.add(line);
                first = false;
            }
        });

        if (!echoComponents.isEmpty())
            echo(echoRepeater, echoComponents.toArray(new BaseComponent[0]));
        return ExecutionResult.SUCCESS;
    }

    @Override
    public ExecutionResult doExecute(UserContext context, MessageRepeatable echoRepeater, String[] params) {
        var proxy = ProxyServer.getInstance();
        var players = proxy.getPlayers();
        int onlinePlayers = players.size();

        // response head
        echo(echoRepeater, new ComponentBuilder(String.format(
                "There %s %s %s online%s",
                onlinePlayers == 1 ? "is" : "are",
                onlinePlayers == 0 ? "no" : (onlinePlayers == 1 ? "only one" : onlinePlayers),
                onlinePlayers == 1 ? "player" : "players",
                onlinePlayers == 0 ? "." : ":"
        )).color(ChatColor.WHITE).create());

        // player list
        if (onlinePlayers <= 2) {
            return printWithOldStyle(players, proxy, echoRepeater);
        } else {
            return printWithGroupedStyle(players, proxy, echoRepeater);
        }
    }

    private BaseComponent[] getPlayerPrettyComponent(ProxiedPlayer player) {
        ComponentBuilder prettyBuilder = new ComponentBuilder();

        // build server text
        var serverButton = PrettyComponents.createNavigableServerButton(player.getServer().getInfo().getName());

        // build player text
        var playerDisplay = new TextComponent(" " + player.getName());
        playerDisplay.setColor(ChatColor.WHITE);
        playerDisplay.setUnderlined(false);
        playerDisplay.setHoverEvent(null);

        // build pretty text
        prettyBuilder.append(playerDisplay);
        prettyBuilder.append(new TextComponent(" "));
        prettyBuilder.append(serverButton);
        return prettyBuilder.create();
    }
}
