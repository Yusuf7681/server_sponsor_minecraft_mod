package com.yusufemin.sponsor;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SponsorPopupScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;
    private static final int PANEL_WIDTH = 250;
    private static final int PANEL_PADDING = 16;
    private static final int MAX_TEXT_WIDTH = PANEL_WIDTH - 24;
    private static final int LINE_HEIGHT = 12;

    private final Screen parent;

    public SponsorPopupScreen(Screen parent) {
        super(Component.literal(SponsorConfig.get().popupTitle));
        this.parent = parent;
    }

    @Override
    protected void init() {
        SponsorConfig cfg = SponsorConfig.get();
        int centerX = this.width / 2;

        String desc = cfg.popupDescription.replace("{code}", cfg.discountCode);
        List<String> lines = wrap(desc, MAX_TEXT_WIDTH);

        int contentHeight = LINE_HEIGHT
                + 8
                + lines.size() * LINE_HEIGHT
                + 14
                + BUTTON_HEIGHT * 3 + BUTTON_GAP * 2;

        int panelHeight = contentHeight + PANEL_PADDING * 2;
        int panelTop = Math.max(4, (this.height - panelHeight) / 2);
        int y = panelTop + PANEL_PADDING;

        Button panel = Button.builder(Component.empty(), b -> { })
                .bounds(centerX - PANEL_WIDTH / 2, panelTop, PANEL_WIDTH, panelHeight).build();
        panel.active = false;
        this.addRenderableWidget(panel);

        Component titleText = Component.literal(cfg.popupTitle)
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
        y = addCenteredLine(titleText, centerX, y) + 8;

        for (String line : lines) {
            y = addCenteredLine(styleLine(line, cfg.discountCode), centerX, y);
        }
        y += 14;

        int buttonX = centerX - BUTTON_WIDTH / 2;

        this.addRenderableWidget(Button.builder(Component.literal(cfg.copyAndOpenText), b -> {
            copyToClipboard(cfg.discountCode);
            openUrl(cfg.targetUrl);
            this.onClose();
        }).bounds(buttonX, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_HEIGHT + BUTTON_GAP;

        this.addRenderableWidget(Button.builder(Component.literal(cfg.openOnlyText), b -> {
            openUrl(cfg.targetUrl);
            this.onClose();
        }).bounds(buttonX, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
        y += BUTTON_HEIGHT + BUTTON_GAP;

        this.addRenderableWidget(Button.builder(Component.literal(cfg.cancelText), b -> this.onClose())
                .bounds(buttonX, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
    }

    private Component styleLine(String line, String code) {
        int idx = line.indexOf(code);
        if (idx < 0) {
            return Component.literal(line).withStyle(ChatFormatting.WHITE);
        }
        MutableComponent result = Component.literal(line.substring(0, idx)).withStyle(ChatFormatting.WHITE);
        result.append(Component.literal(code).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        result.append(Component.literal(line.substring(idx + code.length())).withStyle(ChatFormatting.WHITE));
        return result;
    }

    private int addCenteredLine(Component text, int centerX, int y) {
        int textWidth = this.font.width(text);
        this.addRenderableWidget(new StringWidget(centerX - textWidth / 2, y, textWidth, LINE_HEIGHT, text, this.font));
        return y + LINE_HEIGHT;
    }

    private List<String> wrap(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = current.length() == 0 ? word : current + " " + word;
            if (current.length() > 0 && this.font.width(candidate) > maxWidth) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (current.length() > 0) {
            lines.add(current.toString());
        }
        return lines;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private void copyToClipboard(String text) {
        try {
            if (this.minecraft != null) {
                this.minecraft.keyboardHandler.setClipboard(text);
            }
        } catch (Exception e) {
            SponsorModClient.LOGGER.error("Could not copy to clipboard", e);
        }
    }

    public static void openUrl(String url) {
        try {
            URI uri = URI.create(url.trim());
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                SponsorModClient.LOGGER.warn("Invalid URL scheme, not opening: {}", url);
                return;
            }
            String link = uri.toString();
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", link);
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("open", link);
            } else {
                pb = new ProcessBuilder("xdg-open", link);
            }
            pb.start();
        } catch (Exception e) {
            SponsorModClient.LOGGER.error("Could not open URL: {}", url, e);
        }
    }
}
