package org.customserverlist.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelSummary;
import org.customserverlist.client.CustomEntryRow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SelectWorldScreen.class)
abstract class SelectWorldScreenMixin {
    @Shadow private WorldSelectionList list;
    @Shadow private Button selectButton;

    @Inject(method = "init", at = @At("HEAD"))
    private void customserverlist$releaseReplacedIcons(CallbackInfo ci) {
        if (list != null) {
            for (WorldSelectionList.Entry entry : list.children()) {
                if (entry instanceof CustomEntryRow.World row) {
                    row.close();
                }
            }
        }
    }

    @ModifyArg(method = "init", index = 1, at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;"))
    private Button.OnPress customserverlist$handlePrimaryAction(Button.OnPress original) {
        return button -> {
            if (list.getSelected() instanceof CustomEntryRow.World entry) {
                entry.row().open();
            } else {
                original.onPress(button);
            }
        };
    }

    @Inject(method = "updateButtonStatus", at = @At("TAIL"))
    private void customserverlist$updatePrimaryButton(LevelSummary summary, CallbackInfo ci) {
        if (list.getSelected() instanceof CustomEntryRow.World entry) {
            selectButton.setMessage(CustomEntryRow.OPEN_LINK);
            selectButton.active = entry.row().canOpen();
        }
    }
}
