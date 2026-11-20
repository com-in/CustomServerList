package org.customserverlist.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.customserverlist.client.CustomEntryRow;
import org.customserverlist.config.EntryManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerSelectionList.class)
abstract class ServerSelectionListMixin extends ObjectSelectionList<ServerSelectionList.Entry> {
    @Shadow @Final private JoinMultiplayerScreen screen;
    @Shadow @Final private ServerSelectionList.Entry lanHeader;
    @Unique private List<CustomEntryRow.Server> customserverlist$rows;

    protected ServerSelectionListMixin(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }

    @WrapMethod(method = "refreshEntries")
    private void customserverlist$addEntries(Operation<Void> original) {
        var previousSelection = getSelected();
        original.call();
        if (customserverlist$rows == null) {
            EntryManager.reloadForList();
            customserverlist$rows = EntryManager.forScreen("multiplayer").stream()
                    .map(entry -> new CustomEntryRow.Server(entry, screen))
                    .toList();
        }
        if (customserverlist$rows.isEmpty()) {
            return;
        }

        int pinnedIndex = 0;
        int lanIndex = children().indexOf(lanHeader);
        for (CustomEntryRow.Server row : customserverlist$rows) {
            children().add(row.pinned() ? pinnedIndex++ : lanIndex, row);
            lanIndex++;
        }
        clampScrollAmount();
        if (previousSelection instanceof CustomEntryRow.Server) {
            setSelected(previousSelection);
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void customserverlist$releaseIcons(CallbackInfo ci) {
        if (customserverlist$rows != null) {
            customserverlist$rows.forEach(CustomEntryRow.Server::close);
        }
    }
}
