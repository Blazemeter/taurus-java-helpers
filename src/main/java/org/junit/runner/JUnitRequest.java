package org.junit.runner;

public class JUnitRequest {

    public static Request createRequest(String[] args) {
        JUnitCommandLineParseResult parse = JUnitCommandLineParseResult.parse(args);
        return parse.createRequest(Computer.serial());
    }

}
