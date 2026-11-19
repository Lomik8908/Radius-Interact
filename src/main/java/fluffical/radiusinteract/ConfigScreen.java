package fluffical.radiusinteract;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
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
    public Button exclusionMode;
    public Button fromSide;
    public Button sideAir;
    public MaxRangeSlider maxRange;
    public Button close;

    @Override
    protected void init() {
        enabledButton = Button.builder(RadiusInteract.getTranslation("enabled."+String.valueOf(RadiusInteract.isBreaking)), btn -> {
            RadiusInteract.isBreaking = !RadiusInteract.isBreaking;
            btn.setMessage(RadiusInteract.getTranslation("enabled."+String.valueOf(RadiusInteract.isBreaking)));
        }).bounds((width - 150 - 160) / 2, (height - 20) / 2 - 24 * 2, 150, 20).build();
        
        targetsButton = Button.builder(RadiusInteract.getTranslation("targets"), btn -> {
            minecraft.setScreen(new TargetScreen());
        }).bounds((width - 150 + 160) / 2, (height - 20) / 2 - 24 * 2, 150, 20).build();
        

        useBlockButton = Button.builder(RadiusInteract.getTranslation("mode."+String.valueOf(RadiusInteract.config.useBlock)), btn -> {
            RadiusInteract.config.useBlock = !RadiusInteract.config.useBlock;
            btn.setMessage(RadiusInteract.getTranslation("mode."+String.valueOf(RadiusInteract.config.useBlock)));
        }).bounds((width - 150 - 160) / 2, (height - 20) / 2 - 24, 150, 20).build();
        
        farthestButton = Button.builder(RadiusInteract.getTranslation("radiusmode."+String.valueOf(RadiusInteract.config.farthestFirst)), btn -> {
            RadiusInteract.config.farthestFirst = !RadiusInteract.config.farthestFirst;
            btn.setMessage(RadiusInteract.getTranslation("radiusmode."+String.valueOf(RadiusInteract.config.farthestFirst)));
        }).bounds((width - 150 + 160) / 2, (height - 20) / 2 - 24, 150, 20).build();
        farthestButton.setTooltip(Tooltip.create(RadiusInteract.getTranslation("radiusmode.tooltip")));
        

        exclusionMode = Button.builder(RadiusInteract.getTranslation("exclusionMode."+String.valueOf(RadiusInteract.config.exclusionMode)), btn -> {
            RadiusInteract.config.exclusionMode = (RadiusInteract.config.exclusionMode + 1) % 4;
            btn.setMessage(RadiusInteract.getTranslation("exclusionMode."+String.valueOf(RadiusInteract.config.exclusionMode)));
        }).bounds((width - 150 - 160) / 2, (height - 20) / 2, 150, 20).build();
        
        fromSide = Button.builder(RadiusInteract.getTranslation("side."+String.valueOf(RadiusInteract.config.sideOnly)), btn -> {
            RadiusInteract.config.sideOnly = (RadiusInteract.config.sideOnly + 1) % 7;
            btn.setMessage(RadiusInteract.getTranslation("side."+String.valueOf(RadiusInteract.config.sideOnly)));
        }).bounds((width - 150 + 160) / 2, (height - 20) / 2, 150, 20).build();
        fromSide.setTooltip(Tooltip.create(RadiusInteract.getTranslation("side.tooltip")));

        
        sideAir = Button.builder(RadiusInteract.getTranslation("sideair."+String.valueOf(RadiusInteract.config.sideAir)), btn -> {
            RadiusInteract.config.sideAir = !RadiusInteract.config.sideAir;
            btn.setMessage(RadiusInteract.getTranslation("sideair."+String.valueOf(RadiusInteract.config.sideAir)));
        }).bounds((width - 150 - 160) / 2, (height - 20) / 2 + 24, 150, 20).build();
        sideAir.setTooltip(Tooltip.create(RadiusInteract.getTranslation("sideair.tooltip")));
        

        close = Button.builder(Component.translatable("gui.done"), btn -> onClose()).bounds((width - 200) / 2, height - 36, 200, 20).build();
        
        maxRange = new MaxRangeSlider((width - 150 + 160) / 2, (height - 20) / 2 + 24, 150, 20);
        maxRange.setTooltip(Tooltip.create(RadiusInteract.getTranslation("range.tooltip")));

        // maxRange = new EditBox(font, (width - 200) / 2, (height - 20) / 2 + 24 * 2, 200, 20, RadiusInteract.getTranslation("range.narrator")) {
        //     @Override
        //     public void insertText(String input) {
        //         super.insertText(input.replaceAll("[^,0-9-.]", ""));
        //     }
        // };
        // maxRange.setMaxLength(8);
        // maxRange.setResponder(str -> {
        //     try {
        //         RadiusInteract.config.maxRange = Double.parseDouble(str);
        //         maxRange.setTextColor(0xFFFFFFFF);
        //     } catch (NumberFormatException ex) {
        //         maxRange.setTextColor(0xFFFF4040);
        //     }
        // });
        // maxRange.setValue(String.valueOf(RadiusInteract.config.maxRange));

        addRenderableWidget(enabledButton);
        addRenderableWidget(useBlockButton);
        addRenderableWidget(farthestButton);
        addRenderableWidget(exclusionMode);
        addRenderableWidget(fromSide);
        addRenderableWidget(sideAir);
        addRenderableWidget(targetsButton);
        addRenderableWidget(maxRange);

        addRenderableWidget(close);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
        super.render(graphics, mouseX, mouseY, a);
        graphics.drawString(font, title, (width - font.width(title)) / 2, 16, 0xFFFFFFFF, true);
    }
}
