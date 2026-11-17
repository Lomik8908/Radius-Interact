package fluffical.radiusinteract;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;

public class AddTargetScreen extends Screen {
    private Screen parent;
    public AddTargetScreen(Screen backScreen) {
        super(RadiusInteract.getTranslation("screen.addtarget"));
        parent = backScreen;
    }
    private Button addButton;
    private Button cancelButton;
    private EditBox editBox;
    @Override
    protected void init() {
        editBox = new EditBox(font, (width - 300) / 2, (height - 20) / 2 - 8, 300, 20, RadiusInteract.getTranslation("blockid"));
        editBox.setMaxLength(256);
        editBox.setSuggestion(BuiltInRegistries.BLOCK.getKey(BuiltInRegistries.BLOCK.getRandom(RandomSource.create()).map(Holder::value).orElse(Blocks.STONE)).toString());
        addButton = Button.builder(RadiusInteract.getTranslation("add"), btn -> {
            Identifier id = Identifier.parse(editBox.getValue());
            if (BuiltInRegistries.BLOCK.containsKey(id)) {
                RadiusInteract.config.targets.add(id.toString());
            }
            minecraft.setScreenAndShow(parent);
        }).bounds(width / 2 - 150, height / 2 + 8, 140, 20).build();
        cancelButton = Button.builder(Component.translatable("gui.cancel"), btn -> {
            minecraft.setScreenAndShow(parent);
        }).bounds(width / 2 + 10, height / 2 + 8, 140, 20).build();
        editBox.setResponder(string -> {
            
        });
        addRenderableWidget(editBox);
        addRenderableWidget(addButton);
        addRenderableWidget(cancelButton);
    }
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.text(font, title, (width - font.width(title)) / 2 , (height) / 2 - font.lineHeight - 24, 0xFFFFFFFF);
    }
}
