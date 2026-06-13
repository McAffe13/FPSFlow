package dev.fpsflow.updates;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import dev.fpsflow.FPSFlow;
import dev.fpsflow.config.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public final class UpdateChecker {

    private static final UpdateChecker INSTANCE = new UpdateChecker();

    private static final String API_URL =
            "https://api.modrinth.com/v2/project/fpsflow/version?loaders=[\"fabric\"]";

    private static final String USER_AGENT = "FPSFlow/" + FPSFlow.MOD_ID + " (update-checker)";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private UpdateResult lastResult = null;

    private UpdateChecker() {}

    public static UpdateChecker getInstance() {
        return INSTANCE;
    }

    public void checkAsync() {
        if (!ConfigManager.getInstance().getConfig().updateChecker.enabled) return;

        CompletableFuture.supplyAsync(this::fetchLatestVersion)
                .thenAccept(result -> {
                    lastResult = result;
                    if (result.available()) {
                        FPSFlow.LOGGER.info("[FPSFlow] Update available: {} -> {}",
                                result.currentVersion(), result.latestVersion());
                        notifyPlayer(result);
                    }
                })
                .exceptionally(ex -> {
                    FPSFlow.LOGGER.warn("[FPSFlow] Update check failed: {}", ex.getMessage());
                    return null;
                });
    }

    private UpdateResult fetchLatestVersion() {
        String current = FabricLoader.getInstance()
                .getModContainer(FPSFlow.MOD_ID)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("0.0.0");

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return UpdateResult.failed();
            }

            JsonArray versions = JsonParser.parseString(response.body()).getAsJsonArray();
            if (versions.isEmpty()) {
                return UpdateResult.upToDate(current);
            }

            // First entry is the most recent featured version
            var latest = versions.get(0).getAsJsonObject();
            String latestVersion = latest.get("version_number").getAsString();

            String downloadUrl = null;
            JsonArray files = latest.getAsJsonArray("files");
            if (!files.isEmpty()) {
                downloadUrl = files.get(0).getAsJsonObject().get("url").getAsString();
            }

            if (isNewer(latestVersion, current)) {
                return UpdateResult.newVersion(current, latestVersion, downloadUrl);
            }

            return UpdateResult.upToDate(current);
        } catch (Exception e) {
            FPSFlow.LOGGER.debug("[FPSFlow] Update check exception", e);
            return UpdateResult.failed();
        }
    }

    private boolean isNewer(String latest, String current) {
        try {
            SemanticVersion latestSem = SemanticVersion.parse(latest);
            SemanticVersion currentSem = SemanticVersion.parse(current);
            return latestSem.compareTo(currentSem) > 0;
        } catch (VersionParsingException e) {
            return !latest.equals(current);
        }
    }

    private void notifyPlayer(UpdateResult result) {
        // Delay until the player is in-game
        new Thread(() -> {
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException ignored) {}

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                mc.execute(() -> mc.player.sendMessage(
                        Text.literal("[FPSFlow] ")
                                .formatted(Formatting.AQUA)
                                .append(Text.translatable("fpsflow.update.available",
                                        result.currentVersion(), result.latestVersion())
                                        .formatted(Formatting.WHITE)),
                        false
                ));
            }
        }, "fpsflow-update-notify").start();
    }

    public UpdateResult getLastResult() {
        return lastResult;
    }
}
