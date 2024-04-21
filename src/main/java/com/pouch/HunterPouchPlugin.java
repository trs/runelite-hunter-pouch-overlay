package com.pouch;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.*;
import java.util.Deque;

@Slf4j
@PluginDescriptor(
        name = "Hunter Pouch Overlay"
)
public class HunterPouchPlugin extends Plugin {
    @Inject
    private OverlayManager overlayManager;

    @Inject
    private HunterPouchOverlay overlay;

    @Inject
    private ItemManager itemManager;

    @Inject
    private Client client;

    @Inject
    private HunterPouchConfig config;

    private final Item[] currentInventoryItems = new Item[28];

    private final Deque<Integer> useItemIds = new ArrayDeque<>();

    private final Deque<HunterPouchUseItem> pouchItemUses = new ArrayDeque<>();

    private final Deque<HunterPouchMenuClick> pouchMenuClicks = new ArrayDeque<>();

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        if (InventoryID.INVENTORY.getId() != event.getContainerId()) return;

        var items = event.getItemContainer().getItems();

        var menuClick = pouchMenuClicks.pollFirst();
        var itemUse = pouchItemUses.pollFirst();

        for (int i = 0; i < items.length; i++) {
            var item = items[i];
            var previousItem = currentInventoryItems[i];

            if (previousItem != null) {
                // Item removed
                if (item.getId() == -1 && previousItem.getId() != -1) {
                    if (menuClick != null) {
                        if (menuClick.option.equals("Fill")) {
                            if (menuClick.pouch.isContainableItem(previousItem.getId())) {

                                menuClick.pouch.add(1);

                            }
                        }
                    }

                    if (itemUse != null) {
                        if (previousItem.getId() == itemUse.useItemId) {
                            itemUse.pouch.add(1);
                        }
                    }
                }

                // Item added
                if (item.getId() != -1 && previousItem.getId() == -1) {
                    if (menuClick != null) {
                        if (menuClick.option.equals("Empty")) {
                            if (menuClick.pouch.isContainableItem(item.getId())) {

                                menuClick.pouch.subtract(1);

                            }
                        }
                    }

                    if (itemUse != null) {
                        if (item.getId() == itemUse.useItemId) {
                            itemUse.pouch.subtract(1);
                        }
                    }
                }
            }
            currentInventoryItems[i] = item;
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        switch (event.getMenuAction()) {
            case CANCEL: {
                useItemIds.pollFirst();
                break;
            }
            case WIDGET_TARGET: {
                if (!event.getMenuOption().equals("Use")) return;

                useItemIds.add(event.getItemId());
                break;
            }
            case WIDGET_TARGET_ON_WIDGET: {
                var pouch = HunterPouchItem.forItemID(event.getItemId());
                if (pouch == null) return;

                var useItemId = useItemIds.pollFirst();
                if (useItemId == null) return;

                if (!pouch.isContainableItem(useItemId)) return;

                final int tick = client.getTickCount();

                pouchItemUses.add(new HunterPouchUseItem(pouch, tick, useItemId));
                break;
            }
            case CC_OP_LOW_PRIORITY:
            case CC_OP: {
                var pouch = HunterPouchItem.forItemID(event.getItemId());
                if (pouch == null) return;

                final int tick = client.getTickCount();

                var option = event.getMenuOption();
                switch (option) {
                    case "Fill":
                    case "Empty":
                    case "Check":
                        this.pouchMenuClicks.add(new HunterPouchMenuClick(pouch, tick, option));
                        break;
                }
                break;
            }
        }
    }


    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE) return;

        if (this.pouchMenuClicks.isEmpty()) return;

        var message = event.getMessage();
        var menuClick = this.pouchMenuClicks.pollFirst();
        if (menuClick == null) return;
        if (!menuClick.option.equals("Check")) return;

        var pouch = menuClick.pouch;

        if (HunterPouchMessage.matches(HunterPouchMessage.POUCH_FULL, message)) {
            pouch.count = pouch.capacity;
        } else if (HunterPouchMessage.matches(HunterPouchMessage.POUCH_EMPTY, message)) {
            pouch.count = 0;
        } else {
            var matcher = HunterPouchMessage.POUCH_HOLDING.matcher(message);
            if (matcher.matches()) {
                pouch.count = Integer.parseInt(matcher.group(1));
            }
        }
    }

    @Provides
    HunterPouchConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(HunterPouchConfig.class);
    }
}
