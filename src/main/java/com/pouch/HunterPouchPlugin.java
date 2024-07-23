package com.pouch;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ChatMessage;
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

    private final Item[] currentInventoryItems = new Item[28];

    private final Deque<Integer> useItemIds = new ArrayDeque<>();

    private final Deque<HunterPouchUseItem> pouchItemUses = new ArrayDeque<>();

    private final HashMap<String, Deque<HunterPouchMenuClick>> pouchMenuClickActions = new HashMap<>(Map.of(
        "Fill", new ArrayDeque<>(),
        "Empty", new ArrayDeque<>()
    ));

    private final HashMap<String, Deque<HunterPouchMenuClick>> pouchMenuClickMessages = new HashMap<>(Map.of(
        "Fill", new ArrayDeque<>(),
        "Empty", new ArrayDeque<>(),
        "Check", new ArrayDeque<>()
    ));

    private <T> boolean emptyOrNullFilled(ArrayList<T> array) {
        if (array.isEmpty()) return true;
        for (var item : array) {
            if (item != null) return false;
        }
        return true;
    }

    private HunterPouchUseItem popFirstValidItemUse()
    {
        var op = pouchItemUses.pollFirst();
        while (op != null && op.tick < client.getTickCount())
        {
            op = pouchItemUses.pollFirst();
        }
        return op;
    }

    private HunterPouchMenuClick popFirstValidMenuClickAction(String type)
    {
        var op = pouchMenuClickActions.get(type).pollFirst();
        while (op != null && op.tick < client.getTickCount())
        {
            op = pouchMenuClickActions.get(type).pollFirst();
        }
        return op;
    }


    private HunterPouchMenuClick popFirstValidMenuClickMessage(String type)
    {
        var op = pouchMenuClickMessages.get(type).pollFirst();
        while (op != null && op.tick < client.getTickCount())
        {
            op = pouchMenuClickMessages.get(type).pollFirst();
        }
        return op;
    }

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

        var pouchAdd = new ArrayList<Integer>();
        var pouchRemove = new ArrayList<Integer>();
        var pouchUse = new ArrayList<Integer>();

        for (int i = 0; i < items.length; i++) {
            var item = items[i];
            var previousItem = currentInventoryItems[i];

            if (previousItem != null) {
                // Item added to container
                if (item.getId() == -1 && previousItem.getId() != -1) {
                    pouchAdd.add(previousItem.getId());
                    pouchUse.add(previousItem.getId());
                }

                // Item removed from container
                if (item.getId() != -1 && previousItem.getId() == -1) {
                    pouchRemove.add(item.getId());
                }
            } else {
                // Item removed from container
                if (item.getId() != -1) {
                    pouchRemove.add(item.getId());
                }
            }
            currentInventoryItems[i] = item;
        }

        if (!emptyOrNullFilled(pouchAdd)) {
            var fillAction = popFirstValidMenuClickAction("Fill");
            while (fillAction != null) {
                var pouch = fillAction.pouch;

                for (var i = 0; i < pouchAdd.size(); i++) {
                    var addID = pouchAdd.get(i);
                    if (addID == null) continue;

                    if (pouch.isFull()) break;
                    if (pouch.isContainableItem(addID)) {
                        pouch.add(1);
                        pouchAdd.set(i, null);
                    }
                }

                if (emptyOrNullFilled(pouchAdd)) break;

                fillAction = popFirstValidMenuClickAction("Fill");
            }
        }
        pouchAdd.removeIf(Objects::isNull);

        if (!emptyOrNullFilled(pouchRemove)) {
            var emptyAction = popFirstValidMenuClickAction("Empty");
            while (emptyAction != null) {
                var pouch = emptyAction.pouch;

                for (var i = 0; i < pouchRemove.size(); i++) {
                    var removeID = pouchRemove.get(i);
                    if (removeID == null) continue;

                    if (pouch.getCount() == 0) break;
                    if (pouch.isContainableItem(removeID)) {
                        pouch.subtract(1);
                        pouchRemove.set(i, null);
                    }
                }

                if (emptyOrNullFilled(pouchRemove)) break;

                emptyAction = popFirstValidMenuClickAction("Empty");
            }
        }
        pouchRemove.removeIf(Objects::isNull);

        if (!emptyOrNullFilled(pouchUse)) {
            var useAction = popFirstValidItemUse();
            while (useAction != null) {
                var pouch = useAction.pouch;

                for (var i = 0; i < pouchUse.size(); i++) {
                    var useID = pouchUse.get(i);
                    if (useID == null) continue;

                    if (pouch.isFull()) break;
                    if (pouch.isContainableItem(useID)) {
                        pouch.add(1);
                        pouchUse.set(i, null);
                    }
                }

                if (emptyOrNullFilled(pouchUse)) break;

                useAction = popFirstValidItemUse();
            }
        }
        pouchUse.removeIf(Objects::isNull);
    }


//        var menuClick = popFirstValidMenuClickAction();
//        while (menuClick != null) {
//            var pouch = menuClick.pouch;
//
//            if (menuClick.option.equals("Fill")) {
//                var pouchAddArray = pouchAdd.toArray(new Integer[0]);
//                for (var i = 0; i < pouchAddArray.length; i++) {
//                    var addID = pouchAddArray[i];
//                    if (pouch.isFull()) break;
//                    if (pouch.isContainableItem(addID)) {
//                        pouch.add(1);
//                        pouchAdd.remove(i);
//                    }
//                }
//            }
//
//            if (menuClick.option.equals("Empty")) {
//                var pouchRemoveArray = pouchRemove.toArray(new Integer[0]);
//                for (var i = 0; i < pouchRemoveArray.length; i++) {
//                    var removeID = pouchRemoveArray[i];
//                    if (pouch.getCount() == 0) break;
//                    if (pouch.isContainableItem(removeID)) {
//                        pouch.subtract(1);
//                        pouchRemove.remove(i);
//                    }
//                }
//            }

//            if (menuClick.option.equals("Fill")) {
//                var pouch = menuClick.pouch;
//
//                var removeID = pouchRemove.pollFirst();
//                while (removeID != null) {
//                    if (pouch.getCount() == 0) break;
//                    if (pouch.isContainableItem(removeID)) {
//                        pouch.subtract(1);
//                        pouchRemove.pollFirst();
//                    }
//                    removeID = pouchRemove.peekFirst();
//                }
//            }
//        }

//        var itemUse = popFirstValidItemUse();
//        while (itemUse != null) {
//            var pouch = menuClick.pouch;
//
//            var useID = pouchUse.peekFirst();
//            while (useID != null) {
//                if (pouch.isFull()) break;
//                if (itemUse.useItemId == useID) {
//                    pouch.add(1);
//                    pouchUse.pollFirst();
//                }
//
//                useID = pouchUse.peekFirst();
//            }
//
//            itemUse = popFirstValidItemUse();
//        }
//    }

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

                final int tick = client.getTickCount() + 3;

                pouchItemUses.add(new HunterPouchUseItem(pouch, tick, useItemId));
                break;
            }
            case CC_OP_LOW_PRIORITY:
            case CC_OP: {
                var pouch = HunterPouchItem.forItemID(event.getItemId());
                if (pouch == null) return;

                final int tick = client.getTickCount() + 3;

                var option = event.getMenuOption();
                switch (option) {
                    case "Fill":
                    case "Empty":
                        this.pouchMenuClickActions.get(option).add(new HunterPouchMenuClick(pouch, tick, option));
                    case "Check":
                        this.pouchMenuClickMessages.get(option).add(new HunterPouchMenuClick(pouch, tick, option));
                        break;
                }
                break;
            }
        }
    }


    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE) return;

        var message = event.getMessage();

        if (HunterPouchMessage.matches(HunterPouchMessage.POUCH_FULL, message)) {
            var menuClick = popFirstValidMenuClickMessage("Fill");
            while (menuClick != null) {
                menuClick.pouch.count = menuClick.pouch.capacity;

                menuClick = popFirstValidMenuClickMessage("Fill");
            }
        }
        else if (HunterPouchMessage.matches(HunterPouchMessage.POUCH_EMPTY, message)) {
            var menuClick = popFirstValidMenuClickMessage("Empty");
            while (menuClick != null) {
                menuClick.pouch.count = 0;

                menuClick = popFirstValidMenuClickMessage("Empty");
            }
        } else {
            var menuClick = popFirstValidMenuClickMessage("Check");
            while (menuClick != null) {
                var matcher = HunterPouchMessage.POUCH_HOLDING.matcher(message);
                if (matcher.matches()) {
                    menuClick.pouch.count = Integer.parseInt(matcher.group(1));
                }

                menuClick = popFirstValidMenuClickMessage("Check");
            }

        }
    }
}
