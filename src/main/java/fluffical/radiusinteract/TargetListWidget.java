package fluffical.radiusinteract;

import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
    public void removeEntry(TargetEntry targetEntry) {
        super.removeEntry(targetEntry);
    }
    public void clearEntries() {
        super.clearEntries();
    }
    @Override
    public void setSelected(@Nullable TargetEntry selected) {
        super.setSelected(selected);
        if (onSelected != null)
            onSelected.accept(selected);
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
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            int x = getX() + 6;
            if (drawnStack != null) {
                graphics.fakeItem(drawnStack, getX() + 2, getY() + 2);
                x += 14;
            }
            graphics.text(minecraft.font, text, x, getY() + 6, 0xFFFFFFFF);
        }
    }
}
