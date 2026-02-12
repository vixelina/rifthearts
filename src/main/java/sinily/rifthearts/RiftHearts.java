package sinily.rifthearts;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;

import java.util.*;

public class RiftHearts implements ClientModInitializer {

    public static final String MOD_ID = "rifthearts";
    public static boolean showHearts = true;

    private static RiftHeartsConfig config;

    private static final Set<String> KEYWORDS = Set.of(
            "Stillgore", "Oubliette", "Time Ch", "Wizard Br"
    );

    @Override
    public void onInitializeClient() {
        config = RiftHeartsConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> updateHeartVisibility());

        // --- client commands: /rifthearts [auto|on|off] ---
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("rifthearts")
                        .executes(ctx -> {
                            cycleMode();
                            ctx.getSource().sendFeedback(
                                    Text.literal("§aRiftHearts mode: §e" + config.heartMode));
                            return 1;
                        })
                        .then(ClientCommandManager.literal("auto").executes(ctx -> {
                            setMode("auto");
                            ctx.getSource().sendFeedback(
                                    Text.literal("§aRiftHearts mode set to: §eauto"));
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("on").executes(ctx -> {
                            setMode("on");
                            ctx.getSource().sendFeedback(
                                    Text.literal("§aRiftHearts mode set to: §eon"));
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("off").executes(ctx -> {
                            setMode("off");
                            ctx.getSource().sendFeedback(
                                    Text.literal("§aRiftHearts mode set to: §eoff"));
                            return 1;
                        }))
                )
        );
    }

    /* ---- helpers ---- */

    private void setMode(String mode) {
        config.heartMode = mode;
        config.save();
    }

    private void cycleMode() {
        config.heartMode = switch (config.heartMode) {
            case "auto" -> "on";
            case "on"   -> "off";
            default     -> "auto";
        };
        config.save();
    }

    private void updateHeartVisibility() {
        showHearts = switch (config.heartMode) {
            case "off"  -> false;
            case "on"   -> true;
            default     -> checkScoreboard();   // "auto"
        };
    }

    private boolean checkScoreboard() {
        for (String line : getScoreboardLines()) {
            for (String kw : KEYWORDS) {
                if (line.contains(kw)) return true;
            }
        }
        return false;
    }

    private List<String> getScoreboardLines() {
        List<String> lines = new ArrayList<>();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return lines;

        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective objective =
                scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (objective == null) return lines;

        for (ScoreboardEntry entry : scoreboard.getScoreboardEntries(objective)) {
            if (entry.hidden()) continue;

            Team team = scoreboard.getScoreHolderTeam(entry.owner());
            Text decorated = Team.decorateName(team, entry.name());
            lines.add(decorated.getString());
        }
        return lines;
    }
}