package com.pouch;

public class HunterPouchMenuClick {

    public final HunterPouchItem pouch;
    public final int tick;
    public final String option;

    HunterPouchMenuClick( HunterPouchItem pouch, int tick, String option) {
        this.pouch = pouch;
        this.tick = tick;
        this.option = option;
    }
}
