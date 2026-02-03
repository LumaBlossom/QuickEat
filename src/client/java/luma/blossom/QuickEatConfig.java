package luma.blossom;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "quickeat")
public class QuickEatConfig implements ConfigData {
    public enum SortMode {
        saturation,
        hunger
    }

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public SortMode sortMode = SortMode.saturation;

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    public boolean eatUntilFull = true;

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.Tooltip
    public boolean enableLogs = true;

    @ConfigEntry.Category("logs")
    @ConfigEntry.Gui.Tooltip
    public boolean logStart = true;

    @ConfigEntry.Category("logs")
    @ConfigEntry.Gui.Tooltip
    public boolean logEat = true;

    @ConfigEntry.Category("logs")
    @ConfigEntry.Gui.Tooltip
    public boolean logFull = true;

    @ConfigEntry.Category("logs")
    @ConfigEntry.Gui.Tooltip
    public boolean logError = true;
}
