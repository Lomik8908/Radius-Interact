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
    public boolean removeEntry(TargetEntry entry) {
        return super.removeEntry(entry);
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
        public void render(GuiGraphics graphics, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int xr = x;
            if (drawnStack != null) {
                graphics.renderFakeItem(drawnStack, xr, y);
                xr += 18;
            }
            graphics.drawString(minecraft.font, text, xr, y + 4, 0xFFFFFFFF);
        }
    }
}
