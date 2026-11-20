package org.customserverlist.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelSummary;
import org.customserverlist.client.CustomEntryRow;
import org.customserverlist.config.CustomEntry;
import org.customserverlist.config.EntryManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Mixin(WorldSelectionList.class)
abstract class WorldSelectionListMixin extends ObjectSelectionList<WorldSelectionList.Entry> {
    @Shadow @Final private SelectWorldScreen screen;
    @Unique private List<CustomEntry> customserverlist$entries;

    protected WorldSelectionListMixin(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }

    @Unique
    private List<CustomEntry> customserverlist$entries() {
        if (customserverlist$entries == null) {
            EntryManager.reloadForList();
            customserverlist$entries = EntryManager.forScreen("singleplayer");
        }
        return customserverlist$entries;
    }

    @Inject(method = "loadLevels", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;openFresh(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/Screen;)V"),
            cancellable = true)
    private void customserverlist$allowEntriesWithoutWorlds(CallbackInfoReturnable<CompletableFuture<List<LevelSummary>>> cir) {
        if (!customserverlist$entries().isEmpty()) {
            cir.setReturnValue(CompletableFuture.completedFuture(List.of()));
        }
    }

    @Inject(method = "fillLevels", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/worldselection/WorldSelectionList;notifyListUpdated()V"))
    private void customserverlist$addEntries(String filter, List<LevelSummary> levels, CallbackInfo ci) {
        String query = filter.toLowerCase(Locale.ROOT);
        int pinnedIndex = 0;
        for (CustomEntry entry : customserverlist$entries()) {
            if (!entry.name.toLowerCase(Locale.ROOT).contains(query) && !entry.id.contains(query)) {
                continue;
            }
            var row = new CustomEntryRow.World(entry, screen);
            if (entry.pinned) {
                children().add(pinnedIndex++, row);
            } else {
                addEntry(row);
            }
        }
    }

    @WrapMethod(method = "updateFilter")
    private void customserverlist$resetPrimaryButtonOnFilter(String filter, Operation<Void> original) {
        boolean selectedCustomEntry = getSelected() instanceof CustomEntryRow.World;
        original.call(filter);
        if (selectedCustomEntry && getSelected() == null) {
            setFocused(null);
            screen.updateButtonStatus(null);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void customserverlist$openWithKeyboard(int key, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (CommonInputs.selected(key) && getSelected() instanceof CustomEntryRow.World entry) {
            entry.row().open();
            cir.setReturnValue(true);
        }
    }
}
