/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package com.chromeware.update;

import com.chromeware.update.ModVersion;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MissedReleases {
    private static final Pattern CURSE_FILE = Pattern.compile("^chromeware-(.+)\\.jar$", 2);

    public static List<Candidate> parseModrinth(String json, String minecraftVersion) {
        JsonElement root = JsonParser.parseString((String)json);
        if (!root.isJsonArray()) {
            return List.of();
        }
        ArrayList<Candidate> candidates = new ArrayList<Candidate>();
        for (JsonElement element : root.getAsJsonArray()) {
            String status;
            JsonObject version;
            if (!element.isJsonObject() || !MissedReleases.contains(version = element.getAsJsonObject(), "loaders", "neoforge") || !MissedReleases.contains(version, "game_versions", minecraftVersion) || (status = MissedReleases.stringValue(version, "status")) != null && !status.equals("listed") && !status.equals("archived")) continue;
            MissedReleases.addCandidate(candidates, MissedReleases.stringValue(version, "version_number"), Source.MODRINTH, MissedReleases.stringValue(version, "changelog"), 0L);
        }
        return candidates;
    }

    public static List<Candidate> parseCurseForge(String json, String minecraftVersion) {
        JsonElement root = JsonParser.parseString((String)json);
        if (!root.isJsonObject()) {
            return List.of();
        }
        JsonElement files = root.getAsJsonObject().get("files");
        if (!(files instanceof JsonArray)) {
            return List.of();
        }
        JsonArray array = (JsonArray)files;
        ArrayList<Candidate> candidates = new ArrayList<Candidate>();
        for (JsonElement element : array) {
            JsonObject file;
            if (!element.isJsonObject() || !MissedReleases.contains(file = element.getAsJsonObject(), "versions", minecraftVersion) || !MissedReleases.contains(file, "versions", "NeoForge")) continue;
            String name = MissedReleases.stringValue(file, "name");
            if (name == null) {
                name = MissedReleases.stringValue(file, "display");
            }
            if (name == null || name.endsWith("-thin.jar") || name.endsWith("-sources.jar") || name.endsWith("-javadoc.jar")) continue;
            Matcher matcher = CURSE_FILE.matcher(name);
            JsonElement id = file.get("id");
            if (!matcher.matches()) continue;
            MissedReleases.addCandidate(candidates, matcher.group(1), Source.CURSEFORGE, null, id != null && id.isJsonPrimitive() && id.getAsJsonPrimitive().isNumber() ? id.getAsLong() : 0L);
        }
        return candidates;
    }

    public static Optional<Result> newestAbove(ModVersion installed, List<Candidate> candidates) {
        List<Candidate> newer = candidates.stream().filter(c -> c.version().compareTo(installed) > 0).sorted(Comparator.comparing(Candidate::version)).toList();
        if (newer.isEmpty()) {
            return Optional.empty();
        }
        ArrayList<Release> releases = new ArrayList<Release>();
        ModVersion version = null;
        String changelog = "";
        EnumSet<Source> sources = EnumSet.noneOf(Source.class);
        for (Candidate candidate : newer) {
            if (version != null && candidate.version().compareTo(version) != 0) {
                releases.add(MissedReleases.release(version, sources, changelog));
                sources = EnumSet.noneOf(Source.class);
                changelog = "";
                version = candidate.version();
            }
            if (version == null) {
                version = candidate.version();
            }
            sources.add(candidate.source());
            if (!changelog.isBlank() || candidate.changelog() == null) continue;
            changelog = candidate.changelog();
        }
        releases.add(MissedReleases.release(version, sources, changelog));
        return Optional.of(new Result(installed, version, releases));
    }

    private static Release release(ModVersion version, Set<Source> sources, String changelog) {
        Object title = "Version " + version.getFriendlyString();
        String first = changelog.stripLeading().lines().findFirst().orElse("");
        if (first.startsWith("# ")) {
            title = first.substring(2).trim();
        }
        return new Release(version, sources, (String)title, changelog);
    }

    private static void addCandidate(List<Candidate> candidates, String value, Source source, String changelog, long file) {
        if (value == null) {
            return;
        }
        try {
            candidates.add(new Candidate(ModVersion.parseSemantic(value.trim()), source, changelog, file));
        }
        catch (ModVersion.ParseException parseException) {
            // empty catch block
        }
    }

    private static boolean contains(JsonObject object, String key, String expected) {
        JsonElement value = object.get(key);
        if (!(value instanceof JsonArray)) {
            return false;
        }
        JsonArray array = (JsonArray)value;
        for (JsonElement entry : array) {
            if (!entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isString() || !entry.getAsString().equalsIgnoreCase(expected)) continue;
            return true;
        }
        return false;
    }

    private static String stringValue(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString() ? value.getAsString() : null;
    }

    public static enum Source {
        MODRINTH,
        CURSEFORGE;

    }

    public record Candidate(ModVersion version, Source source, String changelog, long file) {
    }

    public record Release(ModVersion version, Set<Source> sources, String title, String changelog) {
        public Release {
            sources = Set.copyOf(sources);
            changelog = changelog == null ? "" : changelog;
        }
    }

    public record Result(ModVersion installedVersion, ModVersion newestVersion, List<Release> releases) {
        public Result {
            releases = List.copyOf(releases);
        }
    }
}

