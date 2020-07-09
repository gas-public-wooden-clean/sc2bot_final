package com.ebeane.sc2bot;

import com.github.ocraft.s2client.bot.S2Agent;

public class Meatbag extends S2Agent {
    public Meatbag(boolean realtime) {
        this.realtime = realtime;
    }

    private boolean realtime;

    @Override
    public void onStep() {
        if (!realtime) {
            try {
                Thread.sleep(27);
            } catch (InterruptedException ex) {
            }
        }
    }
}
