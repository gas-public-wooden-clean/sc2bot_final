package com.ebeane.sc2bot;

import com.ebeane.sc2bot.CommandLineParseException;

public class CommandLineArguments {
    public CommandLineArguments(String[] args) {
        ArgsIterator iter = new ArgsIterator(args);
        while (iter.moveNext()) {
            if (iter.current().equals("--GamePort") || iter.current().equals("-g")) {
                iter.moveNext();
                try {
                    gamePort = Short.parseShort(iter.current());
                } catch (NumberFormatException ex) {
                    throw new CommandLineParseException("Invalid short " + iter.current() + ".");
                }
            }
            else if (iter.current().equals("--StartPort") || iter.current().equals("-o")) {
                iter.moveNext();
                try {
                    startPort = Integer.parseInt(iter.current());
                } catch (NumberFormatException ex) {
                    throw new CommandLineParseException("Invalid short " + iter.current() + ".");
                }
            }
            else if (iter.current().equals("--LadderServer") || iter.current().equals("-l")) {
                iter.moveNext();
                ladderServer = iter.current();
            }
            else if (iter.current().equals("--ComputerOpponent") || iter.current().equals("-c")) {
            }
            else if (iter.current().equals("--ComputerRace") || iter.current().equals("-a")) {
            }
            else if (iter.current().equals("--ComputerDifficulty") || iter.current().equals("-d")) {
            }
        }
    }

    private short gamePort;
    private Integer startPort;
    private String ladderServer;
    private boolean computerOpponent;

    public short getGamePort() {
        return gamePort;
    }

    public Integer getStartPort() {
        return startPort;
    }

    public String getLadderServer() {
        return ladderServer;
    }

    public boolean getComputerOpponent() {
        return computerOpponent;
    }

    private class ArgsIterator {
        public ArgsIterator(String[] args) {
            this.args = args;
            currentIndex = -1;
        }

        private String[] args;
        private int currentIndex;

        public boolean moveNext() {
            return ++currentIndex < args.length;
        }

        public String current() {
            if (currentIndex < 0 || currentIndex >= args.length) {
                throw new CommandLineParseException("Expected more arguments.");
            }

            return args[currentIndex];
        }
    }
}
