package com.example.rifthearts;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod(modid = RiftHearts.MODID, version = RiftHearts.VERSION, guiFactory = "com.example.rifthearts.RiftHearts$GuiFactory")
public class RiftHearts {
    public static final String MODID = "rifthearts";
    public static final String VERSION = "1.2";

    private static final String CONFIG_CATEGORY = "general";
    private static final String CONFIG_HEART_MODE = "heartMode";

    public static Configuration config;
    private String heartMode;
    private boolean showHearts = true;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        if (config.hasChanged()) {
            config.save();
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            updateHeartVisibility();
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.HEALTH) {
            event.setCanceled(!showHearts);
        }
    }

    private void updateHeartVisibility() {
        heartMode = config.getString(CONFIG_HEART_MODE, CONFIG_CATEGORY, "auto",
                "Heart visibility mode (auto, off, on)", new String[] { "auto", "off", "on" });

        if (heartMode.equals("auto")) {
            showHearts = checkScoreboard();
        } else if (heartMode.equals("off")) {
            showHearts = false;
        } else if (heartMode.equals("on")) {
            showHearts = true;
        }
    }

    private boolean checkScoreboard() {
        List<String> scoreboardLines = getScoreboardLines();
        Set<String> keywords = new HashSet<>(Arrays.asList("Stillgore", "Oubliette", "Time Ch", "Wizard Br"));

        return scoreboardLines.stream().anyMatch(line -> keywords.stream().anyMatch(line::contains));
    }

    private List<String> getScoreboardLines() {
        List<String> lines = new ArrayList<>();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld != null && mc.theWorld.getScoreboard() != null) {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
            if (objective != null) {
                Collection<Score> scores = scoreboard.getSortedScores(objective);
                for (Score score : scores) {
                    ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
                    String line = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName());
                    line = net.minecraft.util.StringUtils.stripControlCodes(line);
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    public static class GuiFactory implements IModGuiFactory {
        @Override
        public void initialize(Minecraft minecraftInstance) {
        }

        @Override
        public Class<? extends GuiScreen> mainConfigGuiClass() {
            return RiftHeartsGuiConfig.class;
        }

        @Override
        public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
            return null;
        }

        @Override
        public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
            return null;
        }
    }

    public static class RiftHeartsGuiConfig extends GuiConfig {
        public RiftHeartsGuiConfig(GuiScreen parentScreen) {
            super(parentScreen,
                    new ConfigElement(RiftHearts.config.getCategory(Configuration.CATEGORY_GENERAL))
                            .getChildElements(),
                    RiftHearts.MODID,
                    false,
                    false,
                    "RiftHearts Configuration");
        }
    }
}
