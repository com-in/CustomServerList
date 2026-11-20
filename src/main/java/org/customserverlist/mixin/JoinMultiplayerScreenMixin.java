package org.customserverlist.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;
import org.customserverlist.client.CustomEntryRow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
abstract class JoinMultiplayerScreenMixin {
    @Shadow protected ServerSelectionList serverSelectionList;
    @Shadow private Button selectButton;

    @Inject(method = "onSelectedChange", at = @At("TAIL"))
    private void customserverlist$updatePrimaryButton(CallbackInfo ci) {
        if (serverSelectionList.getSelected() instanceof CustomEntryRow.Server entry) {
            selectButton.setMessage(CustomEntryRow.OPEN_LINK);
            selectButton.active = entry.row().canOpen();
        } else if (selectButton.getMessage() == CustomEntryRow.OPEN_LINK) {
            selectButton.setMessage(Component.translatable("selectServer.select"));
        }
    }

    @Inject(method = "joinSelectedServer", at = @At("HEAD"), cancellable = true)
    private void customserverlist$openEntry(CallbackInfo ci) {
        if (serverSelectionList.getSelected() instanceof CustomEntryRow.Server entry) {
            entry.row().open();
            ci.cancel();
        }
    }
}
