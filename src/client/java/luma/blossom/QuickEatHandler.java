package luma.blossom;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class QuickEatHandler {
    private static QuickEatHandler instance;
    private EatState state = EatState.IDLE;
    private int originalSlot = -1;
    private int cooldownTicks = 0;
    private int inventoryFoodSlot = -1;
    private int targetHotbarSlot = -1;
    private boolean wasUsingItem = false;
    private boolean swappedItems = false;
    private boolean shouldContinueEating = false;
    private int itemsEaten = 0;
    private int lastFoodLevel = -1;
    private int lastItemCount = -1;

    public enum EatState {
        IDLE,
        SWITCHING,
        EATING,
        COOLDOWN,
        RESTORING
    }

    public enum LogType {
        START,
        EAT,
        FULL,
        ERROR
    }

    public static QuickEatHandler getInstance() {
        if (instance == null) {
            instance = new QuickEatHandler();
        }
        return instance;
    }

    public void startEating(Minecraft client) {
        startEating(client, false);
    }

    public void startEating(Minecraft client, boolean isChaining) {
        if (client.player == null) return;
        
        if (!isChaining && state != EatState.IDLE) {
            return;
        }

        if (!client.player.getFoodData().needsFood()) {
            if (!isChaining) {
                sendMessage(client, "already full", LogType.FULL);
            } else {
                sendMessage(client, "full", LogType.FULL);
                restoreSlots(client);
                reset();
            }
            return;
        }

        Inventory inventory = client.player.getInventory();
        if (!isChaining) {
            originalSlot = inventory.selected;
            QuickEatConfig config = QuickEatClient.getConfig();
            shouldContinueEating = config.eatUntilFull;
            itemsEaten = 0;
            lastFoodLevel = client.player.getFoodData().getFoodLevel();
            
            sendMessage(client, "starting", LogType.START);
        }

        int bestSlot = findBestFoodSlot(client.player);
        if (bestSlot == -1) {
            sendMessage(client, "no more food", LogType.ERROR);
            if (isChaining) {
                restoreSlots(client);
                reset();
            }
            return;
        }

        if (bestSlot < 9) {
            targetHotbarSlot = bestSlot;
            inventoryFoodSlot = -1;
            swappedItems = false;
        } else {
            targetHotbarSlot = originalSlot;
            inventoryFoodSlot = bestSlot;
            swappedItems = true;
        }

        proceedToSwitching(client);
    }

    public void tick(Minecraft client) {
        if (state == EatState.IDLE || client.player == null) {
            return;
        }

        Inventory inventory = client.player.getInventory();
        
        if (state != EatState.RESTORING && state != EatState.COOLDOWN) {
            if (inventory.selected != targetHotbarSlot) {
                sendMessage(client, "cancelled", LogType.ERROR);
                stopEating(client);
                restoreSlots(client);
                reset();
                return;
            }
        }

        switch (state) {

            case SWITCHING:
                handleSwitching(client);
                break;
            case EATING:
                handleEating(client);
                break;
            case COOLDOWN:
                handleCooldown(client);
                break;
            case RESTORING:
                handleRestoring(client);
                break;
            default:
                break;
        }
    }

    private void proceedToSwitching(Minecraft client) {
        Inventory inventory = client.player.getInventory();
        
        if (swappedItems && inventoryFoodSlot != -1) {
            swapSlots(client, targetHotbarSlot, inventoryFoodSlot);
        } else {
            if (inventory.selected != targetHotbarSlot) {
                inventory.selected = targetHotbarSlot;
            }
        }
        
        state = EatState.SWITCHING;
    }

    private void handleSwitching(Minecraft client) {
        state = EatState.EATING;
        wasUsingItem = false;
        ItemStack heldItem = client.player.getMainHandItem();
        lastItemCount = heldItem.getCount();
        startEatingAction(client);
    }

    private void handleEating(Minecraft client) {
        if (client.player == null) {
            reset();
            return;
        }

        boolean currentlyUsing = client.player.isUsingItem();
        int currentFoodLevel = client.player.getFoodData().getFoodLevel();
        ItemStack heldItem = client.player.getMainHandItem();
        int currentItemCount = heldItem.getCount();
        
        if (lastItemCount > 0 && currentItemCount < lastItemCount) {
            itemsEaten++;
            lastItemCount = currentItemCount;
            lastFoodLevel = currentFoodLevel;
            
            sendMessage(client, "ate item #" + itemsEaten, LogType.EAT);
            
            if (!shouldContinueEating && itemsEaten >= 1) {
                stopEating(client);
                sendMessage(client, "done", LogType.FULL);
                state = EatState.RESTORING;
                return;
            }
        }
        
        if (wasUsingItem && !currentlyUsing) {
            stopEating(client);
            
            if (shouldContinueEating) {
                state = EatState.COOLDOWN;
                cooldownTicks = 10;
            } else {
                sendMessage(client, "done", LogType.FULL);
                state = EatState.RESTORING;
            }
            return;
        }

        wasUsingItem = currentlyUsing;
        
        if (!currentlyUsing && state == EatState.EATING) {
            startEatingAction(client);
        }
    }

    private void handleCooldown(Minecraft client) {
        cooldownTicks--;
        
        if (cooldownTicks <= 0) {
            int foodLevel = client.player.getFoodData().getFoodLevel();
            
            if (!shouldContinueEating) {
                state = EatState.RESTORING;
                return;
            }
            
            if (client.player.getFoodData().needsFood()) {
                startEating(client, true);
            } else {
                sendMessage(client, "full", LogType.FULL);
                state = EatState.RESTORING;
            }
        }
    }

    private void handleRestoring(Minecraft client) {
        restoreSlots(client);
        reset();
    }

    private void restoreSlots(Minecraft client) {
        if (client.player == null) return;
        
        Inventory inventory = client.player.getInventory();
        
        if (swappedItems && inventoryFoodSlot != -1) {
            swapSlots(client, targetHotbarSlot, inventoryFoodSlot);
        }

        if (originalSlot != -1) {
            inventory.selected = originalSlot;
        }
    }

    private void startEatingAction(Minecraft client) {
        if (client.player != null && client.gameMode != null) {
            ItemStack heldItem = client.player.getMainHandItem();
            FoodProperties foodProperties = heldItem.get(DataComponents.FOOD);
            
            if (foodProperties != null && client.player.canEat(foodProperties.canAlwaysEat())) {
                if (!client.options.keyUse.isDown()) {
                    client.options.keyUse.setDown(true);
                }
            } else {
                stopEating(client);
                state = EatState.RESTORING;
            }
        }
    }

    private void stopEating(Minecraft client) {
        if (client.options != null) {
            client.options.keyUse.setDown(false);
        }
        if (client.player != null) {
            client.player.releaseUsingItem();
            client.player.stopUsingItem();
        }
    }

    private void swapSlots(Minecraft client, int hotbarSlot, int inventorySlot) {
        if (client.gameMode != null && client.player != null) {
            client.gameMode.handleInventoryMouseClick(
                client.player.containerMenu.containerId,
                inventorySlot,
                hotbarSlot,
                net.minecraft.world.inventory.ClickType.SWAP,
                client.player
            );
        }
    }

    private int findBestFoodSlot(Player player) {
        Inventory inventory = player.getInventory();
        QuickEatConfig config = QuickEatClient.getConfig();
        List<Integer> validSlots = new ArrayList<>();

        for (int i = 0; i < 9; i++) {
            if (isGoodFood(inventory.getItem(i), player)) validSlots.add(i);
        }

        if (validSlots.isEmpty()) return -1;

        validSlots.sort(Comparator.comparingDouble(slot -> -getFoodScore(inventory.getItem(slot), config.sortMode)));
        return validSlots.get(0);
    }

    private boolean isGoodFood(ItemStack stack, Player player) {
        FoodProperties props = stack.get(DataComponents.FOOD);
        if (props == null) return false;
        return player.canEat(props.canAlwaysEat());
    }

    private double getFoodScore(ItemStack stack, QuickEatConfig.SortMode mode) {
        FoodProperties props = stack.get(DataComponents.FOOD);
        if (props == null) return 0;
        
        if (mode == QuickEatConfig.SortMode.saturation) {
            return props.nutrition() * props.saturation() * 2.0f;
        } else {
            return props.nutrition();
        }
    }

    private void sendMessage(Minecraft client, String text, LogType type) {
        QuickEatConfig config = QuickEatClient.getConfig();
        
        if (!config.enableLogs) {
            return;
        }

        boolean shouldLog = switch (type) {
            case FULL -> config.logFull;
            case ERROR -> config.logError;
        };

        if (!shouldLog) {
            return;
        }

        if (client.player != null) {
            String cleanText = text.startsWith("quickeat: ") ? text.substring(10) : text;

            Component prefix = Component.literal("ǫᴜɪᴄᴋᴇᴀᴛ").withStyle(style -> style.withColor(0xFF69B4));
            Component separator = Component.literal(" | ").withStyle(net.minecraft.ChatFormatting.GRAY);
            Component message = Component.literal(cleanText).withStyle(net.minecraft.ChatFormatting.GRAY);

            client.player.displayClientMessage(
                Component.empty().append(prefix).append(separator).append(message), 
                true
            );
        }
    }

    private void reset() {
        state = EatState.IDLE;
        originalSlot = -1;
        cooldownTicks = 0;
        inventoryFoodSlot = -1;
        targetHotbarSlot = -1;
        wasUsingItem = false;
        swappedItems = false;
        shouldContinueEating = false;
        itemsEaten = 0;
        lastFoodLevel = -1;
        lastItemCount = -1;
    }

    public boolean isActive() {
        return state != EatState.IDLE;
    }

    public boolean shouldBlockMovement() {
        return false;
    }
}

