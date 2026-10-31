/*
 * Decompiled with CFR 0.152.
 */
package com.chromeware.update;

import java.util.Arrays;
import java.util.regex.Pattern;

public final class ModVersion
implements Comparable<ModVersion> {
    private static final Pattern IDENTIFIER = Pattern.compile("[0-9A-Za-z-]+");
    private final String friendly;
    private final int[] components;
    private final String[] prerelease;

    private ModVersion(String friendly, int[] components, String[] prerelease) {
        this.friendly = friendly;
        this.components = components;
        this.prerelease = prerelease;
    }

    public static ModVersion parseSemantic(String text) throws ParseException {
        if (text == null || text.isEmpty()) {
            throw new ParseException("Version must be a non-empty string!");
        }
        String core = text;
        int build = core.indexOf(43);
        if (build >= 0) {
            core = core.substring(0, build);
        }
        String[] pre = null;
        int dash = core.indexOf(45);
        if (dash >= 0) {
            String preText = core.substring(dash + 1);
            core = core.substring(0, dash);
            for (String identifier : pre = preText.split("\\.", -1)) {
                if (IDENTIFIER.matcher(identifier).matches()) continue;
                throw new ParseException("Invalid pre-release identifier in " + text);
            }
        }
        String[] parts = core.split("\\.", -1);
        int[] numbers = new int[parts.length];
        for (int i = 0; i < parts.length; ++i) {
            try {
                numbers[i] = Integer.parseInt(parts[i]);
            }
            catch (NumberFormatException exception) {
                throw new ParseException("Invalid version component '" + parts[i] + "' in " + text);
            }
            if (numbers[i] >= 0 && !parts[i].startsWith("+")) continue;
            throw new ParseException("Negative version component in " + text);
        }
        return new ModVersion(text, numbers, pre);
    }

    public String getFriendlyString() {
        return this.friendly;
    }

    @Override
    public int compareTo(ModVersion other) {
        int length = Math.max(this.components.length, other.components.length);
        for (int i = 0; i < length; ++i) {
            int b;
            int a = i < this.components.length ? this.components[i] : 0;
            int n = b = i < other.components.length ? other.components[i] : 0;
            if (a == b) continue;
            return Integer.compare(a, b);
        }
        if (this.prerelease == null || other.prerelease == null) {
            if (this.prerelease == null && other.prerelease == null) {
                return 0;
            }
            return this.prerelease == null ? 1 : -1;
        }
        int shared = Math.min(this.prerelease.length, other.prerelease.length);
        for (int i = 0; i < shared; ++i) {
            String a = this.prerelease[i];
            String b = other.prerelease[i];
            boolean aNumeric = ModVersion.isNumeric(a);
            boolean bNumeric = ModVersion.isNumeric(b);
            int comparison = aNumeric && bNumeric ? Long.compare(Long.parseLong(a), Long.parseLong(b)) : (aNumeric != bNumeric ? (aNumeric ? -1 : 1) : a.compareTo(b));
            if (comparison == 0) continue;
            return comparison;
        }
        return Integer.compare(this.prerelease.length, other.prerelease.length);
    }

    private static boolean isNumeric(String value) {
        if (value.isEmpty() || value.length() > 18) {
            return false;
        }
        for (int i = 0; i < value.length(); ++i) {
            if (Character.isDigit(value.charAt(i))) continue;
            return false;
        }
        return true;
    }

    public boolean equals(Object other) {
        ModVersion version;
        return other instanceof ModVersion && this.compareTo(version = (ModVersion)other) == 0;
    }

    public int hashCode() {
        return Arrays.hashCode(this.trimmed()) * 31 + Arrays.hashCode(this.prerelease);
    }

    private int[] trimmed() {
        int end;
        for (end = this.components.length; end > 1 && this.components[end - 1] == 0; --end) {
        }
        return Arrays.copyOf(this.components, end);
    }

    public String toString() {
        return this.friendly;
    }

    public static final class ParseException
    extends Exception {
        public ParseException(String message) {
            super(message);
        }
    }
}

