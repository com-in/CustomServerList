package org.customserverlist.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.customserverlist.client.CustomEntryRow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
abstract class OnlineServerEntryMixin {
    @Shadow @Final private JoinMultiplayerScreen screen;

    // Vanilla uses visible row indexes as saved-server indexes when reordering.
    @Unique
    private int customserverlist$pinnedCount() {
        var list = ((JoinMultiplayerScreenAccessor) screen).customserverlist$getServerSelectionList();
        int count = 0;
        for (ServerSelectionList.Entry entry : list.children()) {
            if (!(entry instanceof CustomEntryRow.Server)) {
                break;
            }
            count++;
        }
        return count;
    }

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int customserverlist$renderServerIndex(int rowIndex) {
        return rowIndex - customserverlist$pinnedCount();
    }

    @ModifyExpressionValue(method = "keyPressed", at = @At(value = "INVOKE",
            target = "Ljava/util/List;indexOf(Ljava/lang/Object;)I"))
    private int customserverlist$keyboardServerIndex(int rowIndex) {
        return rowIndex == -1 ? -1 : rowIndex - customserverlist$pinnedCount();
    }

    // The first indexOf in mouseClicked is for hit testing and uses the visible row.
    @ModifyExpressionValue(method = "mouseClicked", at = @At(value = "INVOKE", ordinal = 1,
            target = "Ljava/util/List;indexOf(Ljava/lang/Object;)I"))
    private int customserverlist$mouseServerIndex(int rowIndex) {
        return rowIndex - customserverlist$pinnedCount();
    }

    @ModifyArg(method = "swap", index = 0, at = @At(value = "INVOKE",
            target = "Ljava/util/List;get(I)Ljava/lang/Object;"))
    private int customserverlist$findMovedServerRow(int serverIndex) {
        return serverIndex + customserverlist$pinnedCount();
    }
}
