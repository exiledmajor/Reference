/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParser
 *  net.minecraft.SharedConstants
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.TitleScreen
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.fml.ModList
 *  net.neoforged.fml.loading.FMLPaths
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.common.NeoForge
 */
package com.chromeware.client.update;

import com.chromeware.Chromeware;
import com.chromeware.client.update.MarkdownText;
import com.chromeware.client.update.UpdateAvailableScreen;
import com.chromeware.update.MissedReleases;
import com.chromeware.update.ModVersion;
import com.chromeware.update.UpdatePrompt;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class UpdateChecker {
    private static final URI CURSEFORGE_API = URI.create("https://api.cfwidget.com/1755743");
    private static final String CURSEFORGE_CHANGELOG = "https://www.curseforge.com/api/v1/mods/1755743/files/%d/change-log";
    private static UpdatePrompt prompt;
    private static boolean registered;

    public static void register() {
        ModVersion installed;
        if (registered) {
            return;
        }
        registered = true;
        Optional metadata = ModList.get().getModContainerById("chromeware");
        if (metadata.isEmpty()) {
            return;
        }
        String installedText = ((ModContainer)metadata.get()).getModInfo().getVersion().toString();
        try {
            installed = ModVersion.parseSemantic(installedText);
        }
        catch (ModVersion.ParseException exception) {
            Chromeware.LOGGER.debug("Skipping update check for non-release Chromeware version {}", (Object)installedText);
            return;
        }
        ModVersion ignored = UpdateChecker.ignored();
        String gameVersion = SharedConstants.getCurrentVersion().getName();
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).followRedirects(HttpClient.Redirect.NORMAL).build();
        URI modrinthApi = URI.create("https://api.modrinth.com/v2/project/cyberpunk2077mod/version?include_changelog=true&loaders=%5B%22neoforge%22%5D&game_versions=%5B%22" + gameVersion + "%22%5D");
        CompletableFuture<List<MissedReleases.Candidate>> modrinth = UpdateChecker.fetch(http, modrinthApi, installedText, json -> MissedReleases.parseModrinth(json, gameVersion), "Modrinth");
        CompletionStage curseforge = UpdateChecker.fetch(http, CURSEFORGE_API, installedText, json -> MissedReleases.parseCurseForge(json, gameVersion), "CurseForge/CFWidget").thenCompose(files -> {
            List<CompletableFuture> withChangelogs = files.stream().map(file -> file.version().compareTo(installed) > 0 && file.file() > 0L ? UpdateChecker.changelog(http, file, installedText) : CompletableFuture.completedFuture(file)).toList();
            return CompletableFuture.allOf((CompletableFuture[])withChangelogs.toArray(CompletableFuture[]::new)).thenApply(done -> withChangelogs.stream().map(CompletableFuture::join).toList());
        });
        prompt = new UpdatePrompt((CompletableFuture<Optional<MissedReleases.Result>>)((CompletableFuture)modrinth.thenCombine(curseforge, (a, b) -> {
            ArrayList<MissedReleases.Candidate> candidates = new ArrayList<MissedReleases.Candidate>((Collection<MissedReleases.Candidate>)a);
            candidates.addAll((Collection<MissedReleases.Candidate>)b);
            return MissedReleases.newestAbove(installed, candidates).filter(update -> ignored == null || update.newestVersion().compareTo(ignored) > 0);
        })).exceptionally(error -> {
            Chromeware.LOGGER.debug("Could not complete the Chromeware update check", error);
            return Optional.empty();
        }));
        NeoForge.EVENT_BUS.addListener(UpdateChecker::tick);
    }

    static void ignore(ModVersion version) {
        try {
            Files.writeString(UpdateChecker.ignoredFile(), (CharSequence)version.getFriendlyString(), new OpenOption[0]);
        }
        catch (IOException exception) {
            Chromeware.LOGGER.debug("Could not save the ignored Chromeware update", (Throwable)exception);
        }
    }

    private static ModVersion ignored() {
        try {
            Path file = UpdateChecker.ignoredFile();
            return Files.exists(file, new LinkOption[0]) ? ModVersion.parseSemantic(Files.readString(file).trim()) : null;
        }
        catch (ModVersion.ParseException | IOException exception) {
            return null;
        }
    }

    private static Path ignoredFile() {
        return FMLPaths.CONFIGDIR.get().resolve("chromeware-ignored-update.txt");
    }

    static CompletableFuture<List<MissedReleases.Candidate>> fetch(HttpClient http, URI uri, String installed, Function<String, List<MissedReleases.Candidate>> parser, String host) {
        return ((CompletableFuture)http.sendAsync(UpdateChecker.request(uri, installed), HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            if (response.statusCode() == 404 || response.statusCode() == 202) {
                return List.of();
            }
            if (response.statusCode() != 200) {
                throw new IllegalStateException(host + " returned HTTP " + response.statusCode());
            }
            return (List)parser.apply((String)response.body());
        })).exceptionally(error -> {
            Chromeware.LOGGER.debug("Could not check {} for Chromeware updates: {}", (Object)host, (Object)error.toString());
            return List.of();
        });
    }

    private static CompletableFuture<MissedReleases.Candidate> changelog(HttpClient http, MissedReleases.Candidate file, String installed) {
        return ((CompletableFuture)http.sendAsync(UpdateChecker.request(URI.create(CURSEFORGE_CHANGELOG.formatted(file.file())), installed), HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            if (response.statusCode() != 200) {
                return file;
            }
            JsonElement body = JsonParser.parseString((String)((String)response.body())).getAsJsonObject().get("changelogBody");
            if (body == null || !body.isJsonPrimitive()) {
                return file;
            }
            return new MissedReleases.Candidate(file.version(), file.source(), MarkdownText.fromHtml(body.getAsString()), file.file());
        })).exceptionally(error -> {
            Chromeware.LOGGER.debug("Could not fetch the CurseForge changelog for {}: {}", (Object)file.version(), (Object)error.toString());
            return file;
        });
    }

    private static HttpRequest request(URI uri, String installed) {
        return HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(8L)).header("Accept", "application/json").header("User-Agent", "Create-Chromeware/" + installed + " (https://modrinth.com/project/cyberpunk2077mod)").GET().build();
    }

    private static void tick(ClientTickEvent.Post event) {
        Screen screen;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null && mc.getOverlay() == null && (screen = mc.screen) instanceof TitleScreen) {
            TitleScreen title = (TitleScreen)screen;
            prompt.poll(true).ifPresent(update -> mc.setScreen((Screen)new UpdateAvailableScreen(title, (MissedReleases.Result)update)));
        }
    }
}

