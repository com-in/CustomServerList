package org.customserverlist.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.customserverlist.CustomServerList;
import org.customserverlist.config.CustomEntry;
import org.customserverlist.config.EntryManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CustomEntryRow {
    private static final ResourceLocation DEFAULT_ICON =
            ResourceLocation.withDefaultNamespace("textures/misc/unknown_server.png");
    public static final Component OPEN_LINK = Component.translatable("chat.link.open");
    private static int nextTextureId;

    private final CustomEntry entry;
    private final Screen screen;
    private ResourceLocation icon;
    private long lastClick = -250L;

    private CustomEntryRow(CustomEntry entry, Screen screen) {
        this.entry = entry;
        this.screen = screen;
    }

    public boolean canOpen() {
        return "open_url".equals(entry.action);
    }

    public void open() {
        if (canOpen()) {
            ConfirmLinkScreen.confirmLinkNow(screen, entry.url, false);
        }
    }

    private void click() {
        long now = Util.getMillis();
        if (now - lastClick < 250L) {
            lastClick = -250L;
            open();
        } else {
            lastClick = now;
        }
    }

    private Component narration() {
        return Component.literal(entry.name)
                .append(" ").append(description(entry.descriptionLine1))
                .append(" ").append(description(entry.descriptionLine2));
    }

    private static String description(String line) {
        return line == null ? "" : line.replace('\r', ' ').replace('\n', ' ');
    }

    private void render(GuiGraphics graphics, int x, int y, int width) {
        if (icon == null) {
            icon = loadIcon();
        }
        graphics.blit(icon, x, y, 32, 32, 0.0F, 0.0F, 64, 64, 64, 64);

        Font font = Minecraft.getInstance().font;
        int textWidth = width - 38;
        graphics.drawString(font, font.plainSubstrByWidth(entry.name, textWidth), x + 35, y + 1, 0xFFFFFF);
        graphics.drawString(font, font.plainSubstrByWidth(description(entry.descriptionLine1), textWidth),
                x + 35, y + 12, 0xAAAAAA);
        graphics.drawString(font, font.plainSubstrByWidth(description(entry.descriptionLine2), textWidth),
                x + 35, y + 21, 0xAAAAAA);
    }

    private ResourceLocation loadIcon() {
        if (entry.icon == null || entry.icon.isBlank()) {
            return DEFAULT_ICON;
        }

        try {
            Path directory = EntryManager.directory().toRealPath();
            Path relative = Path.of(entry.icon);
            Path path = directory.resolve(relative).normalize();
            if (relative.isAbsolute() || !path.startsWith(directory)
                    || !path.toRealPath().startsWith(directory) || !Files.isRegularFile(path)) {
                throw new IOException("Icon must be a file inside the entries directory");
            }

            NativeImage image;
            try (InputStream input = Files.newInputStream(path)) {
                image = NativeImage.read(input);
            }
            if (image.getWidth() != 64 || image.getHeight() != 64) {
                image.close();
                throw new IOException("Icon must be a 64x64 PNG");
            }

            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                    CustomServerList.MODID, "entry_icons/" + nextTextureId++);
            Minecraft.getInstance().getTextureManager().register(texture, new DynamicTexture(image));
            return texture;
        } catch (IOException | IllegalArgumentException e) {
            CustomServerList.LOGGER.warn("Could not load icon for custom entry {}", entry.id, e);
            return DEFAULT_ICON;
        }
    }

    private void close() {
        if (icon != null && !DEFAULT_ICON.equals(icon)) {
            Minecraft.getInstance().getTextureManager().release(icon);
            icon = null;
        }
        lastClick = -250L;
    }

    public static final class World extends WorldSelectionList.Entry {
        private final CustomEntryRow row;

        public World(CustomEntry entry, Screen screen) {
            row = new CustomEntryRow(entry, screen);
        }

        public CustomEntryRow row() {
            return row;
        }

        @Override
        public Component getNarration() {
            return row.narration();
        }

        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int width, int height,
                           int mouseX, int mouseY, boolean hovered, float partialTick) {
            row.render(graphics, x, y, width);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            row.click();
            return true;
        }

        @Override
        public void close() {
            row.close();
        }
    }

    public static final class Server extends ServerSelectionList.Entry {
        private final CustomEntryRow row;

        public Server(CustomEntry entry, Screen screen) {
            row = new CustomEntryRow(entry, screen);
        }

        public CustomEntryRow row() {
            return row;
        }

        public boolean pinned() {
            return row.entry.pinned;
        }

        @Override
        public Component getNarration() {
            return row.narration();
        }

        @Override
        public void render(GuiGraphics graphics, int index, int y, int x, int width, int height,
                           int mouseX, int mouseY, boolean hovered, float partialTick) {
            row.render(graphics, x, y, width);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            row.click();
            return true;
        }

        @Override
        public void close() {
            row.close();
        }
    }
}
