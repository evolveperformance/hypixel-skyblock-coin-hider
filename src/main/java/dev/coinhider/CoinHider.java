package dev.coinhider;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.CustomData;
import com.mojang.authlib.properties.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class CoinHider implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("CoinHider");
    private static final Path CONFIG = FabricLoader.getInstance().getConfigDir().resolve("coinhider.txt");

    /** "Coins", "Coin", "5 Coins", "+1,250 Coins", "1.5k Coins" - formatting codes already stripped. */
    private static final Pattern COIN_NAME =
            Pattern.compile("(?i)^\\s*\\+?\\s*(?:[\\d.,]+\\s*[kmb]?\\s+)?coins?\\s*$");

    public static volatile boolean enabled = true;

    private static final int DUMP_RADIUS = 1000;
    private static final int CHAT_LIMIT = 25;

    /** True while the sidebar scoreboard says SKYBLOCK (Hypixel shows it on every SkyBlock island). */
    public static volatile boolean inSkyBlock = false;
    private static int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        load();
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (++tickCounter % 20 != 0) return; // once per second is plenty
            inSkyBlock = detectSkyBlock(mc);
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(literal("coinhider")
                        .executes(ctx -> {
                            enabled = !enabled;
                            save();
                            ctx.getSource().sendFeedback(Component.literal("Coin Hider: ")
                                    .append(enabled
                                            ? Component.literal("ON").withStyle(ChatFormatting.GREEN)
                                            : Component.literal("OFF").withStyle(ChatFormatting.RED)));
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(literal("dump").executes(ctx -> dump(ctx.getSource())))));
    }

    private static boolean detectSkyBlock(Minecraft mc) {
        if (mc.level == null) return false;
        Objective sidebar = mc.level.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR);
        if (sidebar == null) return false;
        String title = ChatFormatting.stripFormatting(sidebar.getDisplayName().getString());
        return title != null && title.toUpperCase(java.util.Locale.ROOT).contains("SKYBLOCK");
    }

    /** Hot path: called for every entity every frame, keep it cheap. */
    public static boolean shouldHide(Entity entity) {
        return enabled && inSkyBlock && isCoin(entity);
    }

    public static boolean isCoin(Entity entity) {
        if (!(entity instanceof ItemEntity item)) return false;
        ItemStack stack = item.getItem();
        if (!stack.is(Items.PLAYER_HEAD)) return false;
        // Hypixel coin drops: a bare player head - no custom name and no SkyBlock custom_data/ID.
        // Every real SkyBlock item carries custom_data with an "id", so this does not hit real loot.
        if (!stack.has(DataComponents.CUSTOM_NAME) && !stack.has(DataComponents.CUSTOM_DATA)) return true;
        if (matches(stack.getHoverName())) return true;
        Component tag = item.getCustomName();
        return tag != null && matches(tag);
    }

    /** Skin URL of a player head, for the dump (decoded from the base64 "textures" property). */
    private static String texture(ItemStack stack) {
        ResolvableProfile profile = stack.get(DataComponents.PROFILE);
        if (profile == null) return "-";
        for (Property p : profile.partialProfile().properties().get("textures")) {
            try {
                String json = new String(java.util.Base64.getDecoder().decode(p.value()), java.nio.charset.StandardCharsets.UTF_8);
                java.util.regex.Matcher m = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
                return m.find() ? m.group(1) : json;
            } catch (IllegalArgumentException e) {
                return p.value();
            }
        }
        return "-";
    }

    private static boolean matches(Component name) {
        String plain = ChatFormatting.stripFormatting(name.getString());
        return plain != null && COIN_NAME.matcher(plain).matches();
    }

    private static int dump(FabricClientCommandSource source) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return 0;
        List<ItemEntity> items = new java.util.ArrayList<>(
                mc.level.getEntitiesOfClass(ItemEntity.class, mc.player.getBoundingBox().inflate(DUMP_RADIUS)));
        items.sort(java.util.Comparator.comparingDouble(e -> e.distanceToSqr(mc.player)));
        long hidden = items.stream().filter(CoinHider::shouldHide).count();
        source.sendFeedback(Component.literal("Coin Hider: " + items.size() + " dropped item(s) within " + DUMP_RADIUS
                        + " blocks, " + hidden + " hidden (closest " + Math.min(items.size(), CHAT_LIMIT)
                        + " shown, all in latest.log) | SkyBlock detected: " + (inSkyBlock ? "yes" : "no")
                        + " | enabled: " + (enabled ? "yes" : "no"))
                .withStyle(ChatFormatting.GOLD));
        for (int i = 0; i < items.size(); i++) {
            ItemEntity e = items.get(i);
            ItemStack s = e.getItem();
            boolean coin = shouldHide(e);
            CustomData data = s.get(DataComponents.CUSTOM_DATA);
            String sbId = data == null ? "none" : data.copyTag().getStringOr("id", "none");
            String tex = texture(s);
            String skin = tex.length() > 8 ? tex.substring(tex.length() - 8) : tex;
            String line = s.getItem() + " | name='" + s.getHoverName().getString() + "'"
                    + " | id=" + sbId
                    + " | skin=" + skin
                    + " | " + (int) Math.sqrt(e.distanceToSqr(mc.player)) + "m"
                    + " | hidden=" + coin;
            if (i < CHAT_LIMIT) {
                source.sendFeedback(Component.literal(" - " + line).withStyle(coin ? ChatFormatting.GREEN : ChatFormatting.GRAY));
            }
            LOG.info("[dump] {} | texture={} | components={}", line, tex, s.getComponents());
        }
        return Command.SINGLE_SUCCESS;
    }

    private static void load() {
        try {
            if (Files.exists(CONFIG)) enabled = !Files.readString(CONFIG).trim().equalsIgnoreCase("enabled=false");
        } catch (IOException e) {
            LOG.warn("Could not read {}", CONFIG, e);
        }
    }

    private static void save() {
        try {
            Files.writeString(CONFIG, "enabled=" + enabled + System.lineSeparator());
        } catch (IOException e) {
            LOG.warn("Could not write {}", CONFIG, e);
        }
    }
}
