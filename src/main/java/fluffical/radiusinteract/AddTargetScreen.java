package fluffical.radiusinteract;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
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

    protected String getSuggestion(String text) {
        if (text.length() <= 0)
            return BuiltInRegistries.BLOCK.getKey(BuiltInRegistries.BLOCK.getRandom(RandomSource.create()).map(Holder::value).orElse(Blocks.STONE)).toString();
        
        String suggested = "";
        for (Block block : BuiltInRegistries.BLOCK) {
            String key = BuiltInRegistries.BLOCK.getKey(block).toString();
            if (key.startsWith(text))
                suggested = key;
        }
        if (text.length() >= suggested.length())
            return "";
        return suggested.substring(text.length());
    }

    @Override
    protected void init() {
        editBox = new EditBox(font, (width - 300) / 2, (height - 20) / 2, 300, 20, RadiusInteract.getTranslation("blockid")) {
            @Override
            public void insertText(String input) {
                super.insertText(input.replaceAll("[^a-z0-9/:._-]", ""));
            }
        };
        editBox.setMaxLength(256);
        editBox.setSuggestion(getSuggestion(editBox.getValue()));
        editBox.setTextColor(0xFFFF4040);

        addButton = Button.builder(RadiusInteract.getTranslation("add"), btn -> {
            ResourceLocation id = ResourceLocation.parse(editBox.getValue());
            if (BuiltInRegistries.BLOCK.containsKey(id)) {
                RadiusInteract.config.targets.add(id.toString());
            }
            minecraft.setScreen(parent);
        }).bounds(width / 2 - 150, (height - 20) / 2 + 28, 140, 20).build();
        addButton.active = false;

        cancelButton = Button.builder(Component.translatable("gui.cancel"), btn -> {
            minecraft.setScreen(parent);
        }).bounds(width / 2 + 10, (height - 20) / 2 + 28, 140, 20).build();

        editBox.setResponder(string -> {
            if (string.length() > 0 && BuiltInRegistries.BLOCK.containsKey(ResourceLocation.tryParse(string))) {
                editBox.setTextColor(0xFFFFFFFF);
                editBox.setSuggestion(null);
                addButton.active = true;
            } else {
                editBox.setTextColor(0xFFFF4040);
                editBox.setSuggestion(getSuggestion(string));
                addButton.active = false;
            }
        });
        addRenderableWidget(editBox);
        addRenderableWidget(addButton);
        addRenderableWidget(cancelButton);
    }
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        super.render(graphics, mouseX, mouseY, a);
        graphics.drawString(font, title, (width - font.width(title)) / 2 , (height) / 2 - font.lineHeight - 24, 0xFFFFFFFF);
    }
}
