package com.keuin.bungeecross.testutil;

import com.keuin.bungeecross.intercommunicate.user.RepeatableUser;
import com.keuin.bungeecross.mininstruction.dispatcher.InstructionDispatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestableInstructionDispatcher implements InstructionDispatcher {
    private final List<String> commandList = new ArrayList<>();

    @Override
    public void dispatchExecution(String command, RepeatableUser echoRepeater) {
        commandList.add(command);
    }

    @Override
    public void close() {
    }

    public List<String> getCommandList() {
        return Collections.unmodifiableList(commandList);
    }
}
