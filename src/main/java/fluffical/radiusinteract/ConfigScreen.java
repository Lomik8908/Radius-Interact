package fluffical.radiusinteract;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {
    public ConfigScreen() {
        super(RadiusInteract.getTranslation("screen.config"));
    }

    public Button enabledButton;
    public Button useBlockButton;
    public Button farthestButton;
    public Button targetsButton;
    public EditBox maxRange;
    public Button close;

    @Override
    protected void init() {
        enabledButton = Button.builder(RadiusInteract.getTranslation("enabled."+String.valueOf(RadiusInteract.isBreaking)), btn -> {
            RadiusInteract.isBreaking = !RadiusInteract.isBreaking;
            btn.setMessage(RadiusInteract.getTranslation("enabled."+String.valueOf(RadiusInteract.isBreaking)));
        }).bounds((width - 200) / 2, height / 2 - 24 * 2, 200, 20).build();
        
        useBlockButton = Button.builder(RadiusInteract.getTranslation("mode."+String.valueOf(RadiusInteract.config.useBlock)), btn -> {
            RadiusInteract.config.useBlock = !RadiusInteract.config.useBlock;
            btn.setMessage(RadiusInteract.getTranslation("mode."+String.valueOf(RadiusInteract.config.useBlock)));
        }).bounds((width - 200) / 2, height / 2 - 24, 200, 20).build();
        
        farthestButton = Button.builder(RadiusInteract.getTranslation("radiusmode."+String.valueOf(RadiusInteract.config.farthestFirst)), btn -> {
            RadiusInteract.config.farthestFirst = !RadiusInteract.config.farthestFirst;
            btn.setMessage(RadiusInteract.getTranslation("radiusmode."+String.valueOf(RadiusInteract.config.farthestFirst)));
        }).bounds((width - 200) / 2, height / 2, 200, 20).build();
        
        targetsButton = Button.builder(RadiusInteract.getTranslation("targets"), btn -> {
            minecraft.setScreenAndShow(new TargetScreen());
        }).bounds((width - 200) / 2, height / 2 + 24, 200, 20).build();

        close = Button.builder(Component.translatable("gui.done"), btn -> onClose()).bounds((width - 200) / 2, height - 36, 200, 20).build();
        
        maxRange = new EditBox(font, (width - 200) / 2, height / 2 + 24 * 2, 200, 20, RadiusInteract.getTranslation("range.narrator")) {
            @Override
            public void insertText(String input) {
                super.insertText(input.replaceAll("[^,0-9-.]", ""));
            }
        };
        maxRange.setMaxLength(8);
        maxRange.setResponder(str -> {
            try {
                RadiusInteract.config.maxRange = Double.parseDouble(str);
                maxRange.setTextColor(0xFFFFFFFF);
            } catch (NumberFormatException ex) {
                maxRange.setTextColor(0xFFFF4040);
            }
        });
        maxRange.setTooltip(Tooltip.create(RadiusInteract.getTranslation("range.tooltip")));
        maxRange.setValue(String.valueOf(RadiusInteract.config.maxRange));

        addRenderableWidget(enabledButton);
        addRenderableWidget(useBlockButton);
        addRenderableWidget(farthestButton);
        addRenderableWidget(targetsButton);
        addRenderableWidget(maxRange);

        addRenderableWidget(close);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.text(font, title, (width - font.width(title)) / 2, 16, 0xFFFFFFFF, true);
    }
}
