package fluffical.radiusinteract;

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class TargetListWidget extends ObjectSelectionList<TargetListWidget.TargetEntry> {
    public Consumer<TargetEntry> onSelected;
    public TargetListWidget(Minecraft minecraft, int width, int height, int y,  int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }
    public TargetEntry addEntry(String text, String id) {
        TargetEntry entry = new TargetEntry(text, id);
        addEntry(entry);
        return entry;
    }
    @Override
    public void removeEntry(TargetEntry entry) {
        super.removeEntry(entry);
    }
    public void clearEntries() {
        super.clearEntries();
    }
    @Override
    public void setSelected(TargetEntry entry) {
        super.setSelected(entry);
        if (onSelected != null)
            onSelected.accept(entry);
    }
    public class TargetEntry extends ObjectSelectionList.Entry<TargetEntry> {
        public String text;
        public String id;
        public ItemStack drawnStack;
        public TargetEntry(String text, String id) {
            this.text = text;
            this.id = id;
        }
        @Override
        public Component getNarration() {
            return Component.literal(text);
        }
        @Override
        public void renderContent(GuiGraphics graphics, int i, int j, boolean bl, float f) {
            int x = getX() + 6;
            if (drawnStack != null) {
                graphics.renderFakeItem(drawnStack, getX() + 2, getY() + 2);
                x += 14;
            }
            graphics.drawString(minecraft.font, text, x, getY() + 6, 0xFFFFFFFF);
        }
    }
}
