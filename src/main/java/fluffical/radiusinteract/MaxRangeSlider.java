package fluffical.radiusinteract;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class MaxRangeSlider extends AbstractSliderButton {
    public static final double maxValue = 64;
    public static final int stepCount = 4;
    public MaxRangeSlider(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty(), RadiusInteract.config.maxRange / maxValue);
        updateMessage();
    }
    @Override
    protected void applyValue() {
        value = Math.round(value * (maxValue * stepCount)) / (maxValue * stepCount);
        RadiusInteract.config.maxRange = value * maxValue;
    }
    @Override
    protected void updateMessage() {
        if (value == 0)
            setMessage(RadiusInteract.getTranslation("range", RadiusInteract.getTranslation("unlim")));
        else
            setMessage(RadiusInteract.getTranslation("range", value * maxValue));
    }
}
