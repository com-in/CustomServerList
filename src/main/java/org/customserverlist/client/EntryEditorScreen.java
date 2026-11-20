package org.customserverlist.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.customserverlist.CustomServerList;
import org.customserverlist.config.CustomEntry;
import org.customserverlist.config.EntryManager;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public final class EntryEditorScreen extends Screen {
    private final Screen parent;
    private final CustomEntry entry;
    private final boolean creating;
    private String sortOrder;
    private String message = "";
    private List<EditBox> fields = List.of();
    private int titleY;
    private int messageY;

    public EntryEditorScreen(Screen parent, CustomEntry existing) {
        super(Component.literal(existing == null ? "Create entry" : "Edit entry"));
        this.parent = parent;
        this.creating = existing == null;
        this.entry = creating ? new CustomEntry() : existing.copy();
        this.sortOrder = Integer.toString(entry.sortOrder);
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(330, width - 16);
        int left = (width - contentWidth) / 2;
        Component line1 = Component.translatable("customserverlist.editor.description_line_1");
        Component line2 = Component.translatable("customserverlist.editor.description_line_2");
        Component orderLabel = Component.literal("Sort order");
        int labelWidth = Math.max(font.width(orderLabel), Math.max(font.width(line1), font.width(line2))) + 10;
        int x = left + labelWidth;
        int fieldWidth = contentWidth - labelWidth;

        boolean compact = height < 280;
        int fieldStep = compact ? 20 : 24;
        int buttonStep = compact ? 22 : 25;
        int y = Math.max(18, (height - 7 * fieldStep - 3 * buttonStep) / 2);
        titleY = y - 13;

        EditBox id = field(x, y, fieldWidth, Component.literal("ID"), entry.id, value -> entry.id = value);
        id.setEditable(creating);
        EditBox name = field(x, y + fieldStep, fieldWidth, Component.literal("Name"), entry.name, value -> entry.name = value);
        EditBox description1 = field(x, y + 2 * fieldStep, fieldWidth, line1, entry.descriptionLine1, value -> entry.descriptionLine1 = value);
        EditBox description2 = field(x, y + 3 * fieldStep, fieldWidth, line2, entry.descriptionLine2, value -> entry.descriptionLine2 = value);
        EditBox icon = field(x, y + 4 * fieldStep, fieldWidth, Component.literal("Icon"), entry.icon, value -> entry.icon = value);
        icon.setTooltip(Tooltip.create(Component.literal("64x64 PNG path relative to config/customserverlist/entries")));
        EditBox url = field(x, y + 5 * fieldStep, fieldWidth, Component.literal("URL"), entry.url, value -> entry.url = value);
        EditBox order = field(x, y + 6 * fieldStep, fieldWidth, orderLabel, sortOrder, value -> sortOrder = value);
        fields = List.of(id, name, description1, description2, icon, url, order);

        int columnWidth = (contentWidth - 8) / 3;
        int middle = left + columnWidth + 4;
        int right = middle + columnWidth + 4;
        y += 7 * fieldStep;
        button(displayLabel(), left, y, columnWidth, button -> {
            entry.display = switch (entry.display) {
                case "singleplayer" -> "multiplayer";
                case "multiplayer" -> "both";
                default -> "singleplayer";
            };
            button.setMessage(Component.literal(displayLabel()));
        });
        button(actionLabel(), middle, y, columnWidth, button -> {
            entry.action = "none".equals(entry.action) ? "open_url" : "none";
            button.setMessage(Component.literal(actionLabel()));
        });
        toggle("Enabled", right, y, columnWidth, entry.enabled, value -> entry.enabled = value);

        y += buttonStep;
        toggle("Pinned", left, y, columnWidth, entry.pinned, value -> entry.pinned = value);
        toggle("Allow delete", middle, y, columnWidth, entry.allowDelete, value -> entry.allowDelete = value);
        toggle("Allow edit", right, y, columnWidth, entry.allowEdit, value -> entry.allowEdit = value);

        y += buttonStep;
        int half = (contentWidth - 4) / 2;
        button("Save", left, y, half, button -> save());
        button("Cancel", left + half + 4, y, half, button -> onClose());
        messageY = y + 23;
    }

    private EditBox field(int x, int y, int width, Component label, String value, Consumer<String> responder) {
        EditBox box = new EditBox(font, x, y, width, 18, label);
        box.setMaxLength(Math.max(512, value == null ? 0 : value.length()));
        box.setValue(value == null ? "" : value);
        box.setResponder(responder);
        return addRenderableWidget(box);
    }

    private void button(String label, int x, int y, int width, Button.OnPress onPress) {
        addRenderableWidget(Button.builder(Component.literal(label), onPress).bounds(x, y, width, 20).build());
    }

    private void toggle(String label, int x, int y, int width, boolean initialValue, Consumer<Boolean> responder) {
        addRenderableWidget(new CycleButton.Builder<Boolean>(
                value -> Component.literal(label + ": " + value))
                .withValues(true, false)
                .withInitialValue(initialValue)
                .displayOnlyValue()
                .create(x, y, width, 20, Component.literal(label), (button, value) -> responder.accept(value)));
    }

    private String displayLabel() {
        return switch (entry.display) {
            case "singleplayer" -> "Singleplayer";
            case "multiplayer" -> "Multiplayer";
            default -> "Both menus";
        };
    }

    private String actionLabel() {
        return "open_url".equals(entry.action) ? "Action: Open URL" : "Action: None";
    }

    private void save() {
        CustomEntry saved = entry.copy();
        saved.id = saved.id.trim();
        saved.name = saved.name.trim();
        saved.icon = saved.icon == null ? "" : saved.icon.trim();
        saved.url = saved.url == null ? "" : saved.url.trim();

        try {
            saved.sortOrder = Integer.parseInt(sortOrder.trim());
            if (creating) {
                EntryManager.add(saved);
            } else {
                EntryManager.update(saved);
            }
            onClose();
        } catch (NumberFormatException e) {
            message = "Sort order must be an integer";
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            CustomServerList.LOGGER.warn("Could not save entry", e);
            message = e.getMessage() == null ? "Save failed" : e.getMessage();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, titleY, 0xFFFFFF);
        for (EditBox box : fields) {
            Component label = box.getMessage();
            graphics.drawString(font, label, box.getX() - font.width(label) - 6, box.getY() + 5, 0xFFFFFF);
        }
        if (!message.isEmpty()) {
            graphics.drawCenteredString(font, font.plainSubstrByWidth(message, width - 16), width / 2, messageY, 0xFF5555);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
