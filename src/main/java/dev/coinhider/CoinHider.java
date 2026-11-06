package dev.coinhider;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;

import java.util.Locale;

/**
 * Hides Hypixel SkyBlock coin drops (e.g. from the Coin Talisman). Always on while in SkyBlock, no config.
 */
public final class CoinHider implements ClientModInitializer {
    /** True while the sidebar scoreboard says SKYBLOCK (Hypixel shows it on every SkyBlock island). */
    private static volatile boolean inSkyBlock = false;
    private static int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (++tickCounter % 20 != 0) return; // once per second is plenty
            inSkyBlock = detectSkyBlock(mc);
        });
    }

    private static boolean detectSkyBlock(Minecraft mc) {
        if (mc.level == null) return false;
        Objective sidebar = mc.level.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR);
        if (sidebar == null) return false;
        String title = ChatFormatting.stripFormatting(sidebar.getDisplayName().getString());
        return title != null && title.toUpperCase(Locale.ROOT).contains("SKYBLOCK");
    }

    /** Hot path: called for every entity every frame, keep it cheap. */
    public static boolean shouldHide(Entity entity) {
        return inSkyBlock && isCoin(entity);
    }

    /**
     * Hypixel coin drops are bare player heads: no custom name and no SkyBlock custom_data/ID.
     * Every real SkyBlock item carries custom_data with an "id", so real loot is never hidden.
     */
    private static boolean isCoin(Entity entity) {
        if (!(entity instanceof ItemEntity item)) return false;
        ItemStack stack = item.getItem();
        return stack.is(Items.PLAYER_HEAD)
                && !stack.has(DataComponents.CUSTOM_NAME)
                && !stack.has(DataComponents.CUSTOM_DATA);
    }
}
