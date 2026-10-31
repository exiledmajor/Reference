/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.ClickEvent
 *  net.minecraft.network.chat.ClickEvent$Action
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 */
package com.chromeware.client.update;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

final class MarkdownText {
    private MarkdownText() {
    }

    static String withoutTitleHeading(String changelog) {
        String text = changelog.replace("\r\n", "\n").replace('\r', '\n').stripLeading();
        if (!text.startsWith("# ")) {
            return text;
        }
        int newline = text.indexOf(10);
        return newline < 0 ? "" : text.substring(newline + 1).stripLeading();
    }

    static String fromHtml(String html) {
        String text = html.replaceAll("\\s+", " ").replaceAll("(?i)<br\\s*/?>", "\n").replaceAll("(?i)</p>|</div>", "\n\n").replaceAll("(?i)<h1[^>]*>", "\n# ").replaceAll("(?i)<h2[^>]*>", "\n## ").replaceAll("(?i)<h[3-6][^>]*>", "\n### ").replaceAll("(?i)</h[1-6]>", "\n").replaceAll("(?i)<li[^>]*>", "\n- ").replaceAll("(?i)</?(ul|ol)[^>]*>", "\n").replaceAll("(?i)<blockquote[^>]*>", "\n> ").replaceAll("(?i)<hr[^>]*>", "\n---\n").replaceAll("(?i)</?(strong|b)(\\s[^>]*)?>", "**").replaceAll("(?i)</?(em|i)(\\s[^>]*)?>", "*").replaceAll("(?i)</?code[^>]*>", "`").replaceAll("(?i)<a\\s[^>]*href=\"([^\"]*)\"[^>]*>(.*?)</a>", "[$2]($1)").replaceAll("<[^>]+>", "");
        StringBuilder decoded = new StringBuilder();
        Matcher entity = Pattern.compile("&(#x[0-9a-fA-F]+|#[0-9]+|amp|lt|gt|quot|apos|nbsp);").matcher(text);
        while (entity.find()) {
            String name;
            String value = switch (name = entity.group(1)) {
                case "amp" -> "&";
                case "lt" -> "<";
                case "gt" -> ">";
                case "quot" -> "\"";
                case "apos" -> "'";
                case "nbsp" -> " ";
                default -> Character.toString(name.startsWith("#x") ? Integer.parseInt(name.substring(2), 16) : Integer.parseInt(name.substring(1)));
            };
            entity.appendReplacement(decoded, Matcher.quoteReplacement(value));
        }
        entity.appendTail(decoded);
        return decoded.toString().lines().map(String::strip).reduce((a, b) -> a + "\n" + b).orElse("").replaceAll("\n{3,}", "\n\n").strip();
    }

    static Component parse(String markdown) {
        MutableComponent result = Component.empty();
        String[] lines = markdown.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        for (int index = 0; index < lines.length; ++index) {
            Object line = lines[index];
            String stripped = ((String)line).stripLeading();
            int leadingSpaces = ((String)line).length() - stripped.length();
            Style baseStyle = Style.EMPTY;
            if (stripped.startsWith("### ")) {
                line = stripped.substring(4);
                baseStyle = Style.EMPTY.withBold(Boolean.valueOf(true)).withColor(ChatFormatting.GOLD);
            } else if (stripped.startsWith("## ")) {
                line = stripped.substring(3);
                baseStyle = Style.EMPTY.withBold(Boolean.valueOf(true)).withColor(ChatFormatting.GOLD);
            } else if (stripped.startsWith("# ")) {
                line = stripped.substring(2);
                baseStyle = Style.EMPTY.withBold(Boolean.valueOf(true)).withColor(ChatFormatting.GOLD);
            } else if (stripped.startsWith("- ") || stripped.startsWith("* ")) {
                line = "  ".repeat(Math.min(3, leadingSpaces / 2)) + "\u2022 " + stripped.substring(2);
            } else if (stripped.startsWith("> ")) {
                line = stripped.substring(2);
                baseStyle = Style.EMPTY.withItalic(Boolean.valueOf(true)).withColor(ChatFormatting.GRAY);
            } else if (((String)line).matches("^\\s*([-*_])\\1{2,}\\s*$")) {
                line = "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500";
                baseStyle = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY);
            }
            result.append((Component)MarkdownText.parseInline((String)line, baseStyle));
            if (index >= lines.length - 1) continue;
            result.append("\n");
        }
        return result;
    }

    private static MutableComponent parseInline(String line, Style baseStyle) {
        MutableComponent result = Component.empty();
        int cursor = 0;
        while (cursor < line.length()) {
            int end;
            if (line.startsWith("**", cursor) && (end = line.indexOf("**", cursor + 2)) >= 0) {
                result.append((Component)Component.literal((String)line.substring(cursor + 2, end)).setStyle(baseStyle.withBold(Boolean.valueOf(true))));
                cursor = end + 2;
                continue;
            }
            if (line.charAt(cursor) == '*' && (end = line.indexOf(42, cursor + 1)) >= 0) {
                result.append((Component)Component.literal((String)line.substring(cursor + 1, end)).setStyle(baseStyle.withItalic(Boolean.valueOf(true))));
                cursor = end + 1;
                continue;
            }
            if (line.charAt(cursor) == '`' && (end = line.indexOf(96, cursor + 1)) >= 0) {
                result.append((Component)Component.literal((String)line.substring(cursor + 1, end)).setStyle(baseStyle.withColor(ChatFormatting.GOLD)));
                cursor = end + 1;
                continue;
            }
            if (line.charAt(cursor) == '[') {
                int urlEnd;
                int labelEnd = line.indexOf("](", cursor + 1);
                int n = urlEnd = labelEnd < 0 ? -1 : line.indexOf(41, labelEnd + 2);
                if (labelEnd >= 0 && urlEnd >= 0) {
                    String label = line.substring(cursor + 1, labelEnd);
                    String url = line.substring(labelEnd + 2, urlEnd);
                    Style linkStyle = baseStyle.withColor(ChatFormatting.GOLD).withUnderlined(Boolean.valueOf(true));
                    if (MarkdownText.validHttpUrl(url)) {
                        linkStyle = linkStyle.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                    }
                    result.append((Component)Component.literal((String)label).setStyle(linkStyle));
                    cursor = urlEnd + 1;
                    continue;
                }
            }
            int next = MarkdownText.nextMarker(line, cursor + 1);
            result.append((Component)Component.literal((String)line.substring(cursor, next)).setStyle(baseStyle));
            cursor = next;
        }
        return result;
    }

    private static int nextMarker(String line, int start) {
        int next = line.length();
        for (char marker : new char[]{'*', '`', '['}) {
            int index = line.indexOf(marker, start);
            if (index < 0) continue;
            next = Math.min(next, index);
        }
        return next;
    }

    private static boolean validHttpUrl(String value) {
        try {
            String scheme = URI.create(value).getScheme();
            return "https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme);
        }
        catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}

