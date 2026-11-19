package fluffical.radiusinteract;

import fluffical.radiusinteract.TargetListWidget.TargetEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class TargetScreen extends Screen {
    public TargetScreen() {
        super(RadiusInteract.getTranslation("screen.targets"));
    }

    private TargetListWidget blockList;
    private Button clearList;
    private Button addBlock;
    private Button removeBlock;

    @Override
    protected void init() {
        blockList = new TargetListWidget(minecraft, width, height - 60, 20, 20);

        clearList = Button.builder(RadiusInteract.getTranslation("target.clear"), btn -> {
            minecraft.setScreen(new ConfirmScreen(bool -> {
                if (bool) {
                    RadiusInteract.config.targets.clear();
                    blockList.clearEntries();
                }
                minecraft.setScreen(this);
            }, RadiusInteract.getTranslation("targets"), RadiusInteract.getTranslation("clear.msg", RadiusInteract.config.targets.size())));
        }).bounds((width - 96) / 2 - 100, height - 30, 96, 20).build();
        
        addBlock = Button.builder(RadiusInteract.getTranslation("target.add"), btn -> {
            minecraft.setScreen(new AddTargetScreen(this));
        }).bounds((width - 96) / 2, height - 30, 96, 20).build();

        removeBlock = Button.builder(RadiusInteract.getTranslation("target.remove"), btn -> {
            TargetEntry entry = blockList.getSelected();
            if (entry != null)
                minecraft.setScreen(new ConfirmScreen(bool -> {
                    if (bool) {
                        if (RadiusInteract.config.targets.contains(entry.id)) {
                            RadiusInteract.config.targets.remove(entry.id);
                            blockList.removeEntry(entry);
                        }
                    }
                    minecraft.setScreen(this);
                }, RadiusInteract.getTranslation("targets"), RadiusInteract.getTranslation("remove.msg", entry.id)));
        }).bounds((width - 96) / 2 + 100, height - 30, 96, 20).build();
        
        removeBlock.active = false;
        clearList.active = RadiusInteract.config.targets.size() > 0;
        
        blockList.onSelected = target -> {
            removeBlock.active = target != null;
        };
        addRenderableWidget(blockList);
        addRenderableWidget(clearList);
        addRenderableWidget(addBlock);
        addRenderableWidget(removeBlock);
        updateList();
    }

    public void updateList() {
        for (String blockid : RadiusInteract.config.targets) {
            var block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockid));
            if (block != null) {
                TargetEntry entry = blockList.addEntry(blockid, blockid);
                entry.drawnStack = new ItemStack(block);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        guiGraphics.drawString(font, title, (width - font.width(title)) /2 , (20 - font.lineHeight) / 2, 0xFFFFFFFF);
    }
}
