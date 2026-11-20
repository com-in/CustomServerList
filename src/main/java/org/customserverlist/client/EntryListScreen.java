package org.customserverlist.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.customserverlist.CustomServerList;
import org.customserverlist.config.CustomEntry;
import org.customserverlist.config.EntryManager;

import java.io.IOException;
import java.util.List;

public final class EntryListScreen extends Screen {
    private final Screen parent;
    private int page = 0;
    private String message = "";
    private static final int PAGE_SIZE = 5;

    public EntryListScreen(Screen parent) {
        super(Component.literal("CustomServerList - Entries"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        try {
            EntryManager.load();
        } catch (IOException e) {
            CustomServerList.LOGGER.error("Could not load entries", e);
            message = "JSON load failed; check latest.log";
        }
        List<CustomEntry> entries = EntryManager.all();
        int maxPage = Math.max(0, (entries.size() - 1) / PAGE_SIZE);
        page = Math.min(page, maxPage);
        int left = width / 2 - 155;
        int top = 52;
        for (int i = page * PAGE_SIZE; i < Math.min(entries.size(), (page + 1) * PAGE_SIZE); i++) {
            CustomEntry entry = entries.get(i);
            int y = top + (i % PAGE_SIZE) * 29;
            String label = (entry.pinned ? "[PIN] " : "") + entry.name;
            addRenderableWidget(Button.builder(Component.literal(label), b -> open(entry))
                    .bounds(left, y, 174, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Edit"), b ->
                            minecraft.setScreen(new EntryEditorScreen(this, entry)))
                    .bounds(left + 179, y, 58, 20).build()).active = entry.allowEdit;
            addRenderableWidget(Button.builder(Component.literal("Del"), b -> confirmDelete(entry))
                    .bounds(left + 242, y, 58, 20).build()).active = entry.allowDelete;
        }
        int bottom = height - 30;
        addRenderableWidget(Button.builder(Component.literal("<"), b -> { page--; rebuildWidgets(); })
                .bounds(left, bottom, 35, 20).build()).active = page > 0;
        addRenderableWidget(Button.builder(Component.literal(">"), b -> { page++; rebuildWidgets(); })
                .bounds(left + 40, bottom, 35, 20).build()).active = page < maxPage;
        addRenderableWidget(Button.builder(Component.literal("New entry"), b ->
                        minecraft.setScreen(new EntryEditorScreen(this, null)))
                .bounds(left + 85, bottom, 110, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> minecraft.setScreen(parent))
                .bounds(left + 200, bottom, 100, 20).build());
    }

    private void open(CustomEntry entry) {
        if (!"open_url".equals(entry.action)) return;
        ConfirmLinkScreen.confirmLinkNow(this, entry.url, false);
    }

    private void confirmDelete(CustomEntry entry) {
        minecraft.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                try {
                    EntryManager.remove(entry.id);
                    message = "Deleted: " + entry.id;
                } catch (IOException e) {
                    CustomServerList.LOGGER.error("Could not delete entry", e);
                    message = "Delete failed; check latest.log";
                }
            }
            minecraft.setScreen(this);
        }, Component.literal("Delete entry?"), Component.literal(entry.name)));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFFFFF);
        graphics.drawCenteredString(font, "Click an entry to open its URL (if configured)", width / 2, 34, 0xAAAAAA);
        if (!message.isEmpty()) graphics.drawCenteredString(font, message, width / 2, height - 48, 0xFFAA55);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() { minecraft.setScreen(parent); }
}
