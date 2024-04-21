package com.pouch;

import java.awt.*;
import javax.inject.Inject;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.components.TextComponent;

public class HunterPouchOverlay extends WidgetItemOverlay {
    @Inject
    HunterPouchOverlay() {
        showOnInventory();
        showOnBank();
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem) {

        var pouch = HunterPouchItem.forItemID(itemId);
        if (pouch == null) return;

        graphics.setFont(FontManager.getRunescapeSmallFont());
        renderText(graphics, widgetItem.getCanvasBounds(), pouch);
    }

    private void renderText(Graphics2D graphics, Rectangle bounds, HunterPouchItem pouch) {

        final TextComponent textComponent = new TextComponent();
        // top left
        textComponent.setPosition(new Point(bounds.x - 1, bounds.y + 10));

        if (pouch.count >= 0) {
            if (pouch.count == 0) {
                textComponent.setColor(Color.GREEN);
            } else if (pouch.count == pouch.capacity) {
                textComponent.setColor(Color.RED);
            } else {
                textComponent.setColor(Color.YELLOW);
            }

            textComponent.setText(String.valueOf(pouch.count));
        }
        else {
            textComponent.setColor(Color.ORANGE);
            textComponent.setText("?");
        }
        textComponent.render(graphics);
    }
}
