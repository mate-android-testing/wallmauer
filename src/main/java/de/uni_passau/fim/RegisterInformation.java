package de.uni_passau.fim;

import java.util.ArrayList;
import java.util.List;

public class RegisterInformation {

    private final String identifier;

    // refer to register IDs
    private List<Integer> usableRegisters;
    private List<Integer> newLocalRegisters;

    public RegisterInformation(String identifier, List<Integer> newLocalRegisters, List<Integer> usableRegisters) {
        this.identifier = identifier;
        this.newLocalRegisters = newLocalRegisters;
        this.usableRegisters = usableRegisters;
    }

    public List<Integer> getUsableRegisters() {
        return usableRegisters;
    }

    public List<Integer> getNewLocalRegisters() {
        return newLocalRegisters;
    }

    public final String getIdentifier() {
        return identifier;
    }
}
