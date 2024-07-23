package com.pouch;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.ItemID;

public enum HunterPouchItem {
    SMALL_FUR_POUCH(14, HunterPouchType.FUR),
    MEDIUM_FUR_POUCH(21, HunterPouchType.FUR),
    LARGE_FUR_POUCH(28, HunterPouchType.FUR),
    SMALL_MEAT_POUCH(14, HunterPouchType.MEAT),
    LARGE_MEAT_POUCH(28, HunterPouchType.MEAT);

    @Getter(AccessLevel.PACKAGE)
    public final HunterPouchType type;

    @Getter(AccessLevel.PACKAGE)
    public final int capacity;

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    public int count = -1;

    HunterPouchItem(int capacity, HunterPouchType type)
    {
        this.type = type;
        this.capacity = capacity;
    }

    public void set(int value){
        this.count = value;
    }

    public void add(int value) {
        if (this.count == -1) return;
        this.count = Math.min(this.capacity, this.count + value);
    }

    public void subtract(int value) {
        if (this.count == -1)  return;
        this.count = Math.max(0, this.count - value);
    }

    public boolean isFull() {
        return this.count >= this.capacity;
    }

    public boolean isContainableItem(int itemID) {
        switch (itemID) {
            case ItemID.POLAR_KEBBIT_FUR:
            case ItemID.DARK_KEBBIT_FUR:
            case ItemID.COMMON_KEBBIT_FUR:
            case ItemID.SPOTTED_KEBBIT_FUR:
            case ItemID.DASHING_KEBBIT_FUR:
            case ItemID.FELDIP_WEASEL_FUR:
            case ItemID.DESERT_DEVIL_FUR:
            case ItemID.LARUPIA_FUR:
            case ItemID.GRAAHK_FUR:
            case ItemID.KYATT_FUR:
            case ItemID.FOX_FUR:
            case ItemID.SUNLIGHT_ANTELOPE_FUR:
            case ItemID.MOONLIGHT_ANTELOPE_FUR:
                return this.type == HunterPouchType.FUR;
            case ItemID.RAW_BEAST_MEAT:
            case ItemID.RAW_DASHING_KEBBIT:
            case ItemID.RAW_BARBTAILED_KEBBIT:
            case ItemID.RAW_WILD_KEBBIT:
            case ItemID.RAW_GRAAHK:
            case ItemID.RAW_KYATT:
            case ItemID.RAW_LARUPIA:
            case ItemID.RAW_PYRE_FOX:
            case ItemID.RAW_MOONLIGHT_ANTELOPE:
            case ItemID.RAW_SUNLIGHT_ANTELOPE:
                return this.type == HunterPouchType.MEAT;
            default:
                return false;
        }
    }

    public static HunterPouchItem forItemID(int itemID) {
        switch (itemID) {
            case ItemID.SMALL_FUR_POUCH:
            case ItemID.SMALL_FUR_POUCH_OPEN:
                return HunterPouchItem.SMALL_FUR_POUCH;
            case ItemID.MEDIUM_FUR_POUCH:
            case ItemID.MEDIUM_FUR_POUCH_OPEN:
                return HunterPouchItem.MEDIUM_FUR_POUCH;
            case ItemID.LARGE_FUR_POUCH:
            case ItemID.LARGE_FUR_POUCH_OPEN:
                return HunterPouchItem.LARGE_FUR_POUCH;
            case ItemID.SMALL_MEAT_POUCH:
            case ItemID.SMALL_MEAT_POUCH_OPEN:
                return HunterPouchItem.SMALL_MEAT_POUCH;
            case ItemID.LARGE_MEAT_POUCH:
            case ItemID.LARGE_MEAT_POUCH_OPEN:
                return HunterPouchItem.LARGE_MEAT_POUCH;
            default:
                return null;
        }
    }

}

