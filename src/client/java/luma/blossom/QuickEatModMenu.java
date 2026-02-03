package luma.blossom;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class QuickEatModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.quickeat.title"));

            QuickEatConfig config = QuickEatClient.getConfig();
            builder.setSavingRunnable(() -> {
                AutoConfig.getConfigHolder(QuickEatConfig.class).save();
            });

            ConfigCategory general = builder.getOrCreateCategory(Component.translatable("config.quickeat.category.general"));
            
            general.addEntry(builder.entryBuilder()
                .startEnumSelector(Component.translatable("config.quickeat.option.sortMode"), QuickEatConfig.SortMode.class, config.sortMode)
                .setDefaultValue(QuickEatConfig.SortMode.saturation)
                .setTooltip(Component.translatable("config.quickeat.option.sortMode.tooltip"))
                .setSaveConsumer(newValue -> config.sortMode = newValue)
                .build());

            general.addEntry(builder.entryBuilder()
                .startBooleanToggle(Component.translatable("config.quickeat.option.eatUntilFull"), config.eatUntilFull)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.quickeat.option.eatUntilFull.tooltip"))
                .setSaveConsumer(newValue -> config.eatUntilFull = newValue)
                .build());



            ConfigCategory logs = builder.getOrCreateCategory(Component.translatable("config.quickeat.category.logs"));

            logs.addEntry(builder.entryBuilder()
                .startBooleanToggle(Component.translatable("config.quickeat.option.enableLogs"), config.enableLogs)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.quickeat.option.enableLogs.tooltip"))
                .setSaveConsumer(newValue -> config.enableLogs = newValue)
                .build());

            logs.addEntry(builder.entryBuilder()
                .startBooleanToggle(Component.translatable("config.quickeat.option.logStart"), config.logStart)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.quickeat.option.logStart.tooltip"))
                .setSaveConsumer(newValue -> config.logStart = newValue)
                .build());

            logs.addEntry(builder.entryBuilder()
                .startBooleanToggle(Component.translatable("config.quickeat.option.logEat"), config.logEat)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.quickeat.option.logEat.tooltip"))
                .setSaveConsumer(newValue -> config.logEat = newValue)
                .build());

            logs.addEntry(builder.entryBuilder()
                .startBooleanToggle(Component.translatable("config.quickeat.option.logFull"), config.logFull)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.quickeat.option.logFull.tooltip"))
                .setSaveConsumer(newValue -> config.logFull = newValue)
                .build());

            logs.addEntry(builder.entryBuilder()
                .startBooleanToggle(Component.translatable("config.quickeat.option.logError"), config.logError)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.quickeat.option.logError.tooltip"))
                .setSaveConsumer(newValue -> config.logError = newValue)
                .build());

            return builder.build();
        };
    }
}
