package com.pouch;

public class HunterPouchUseItem {
    public final HunterPouchItem pouch;
    public final int tick;
    public final int useItemId;

    HunterPouchUseItem( HunterPouchItem pouch, int tick, int useItemId) {
        this.pouch = pouch;
        this.tick = tick;
        this.useItemId = useItemId;
    }
}
