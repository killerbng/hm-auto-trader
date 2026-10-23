package dev.hm.autotrader.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read the vanilla container GUI origin without relying on nonexistent getters. */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    int hm$getLeftPos();

    @Accessor("topPos")
    int hm$getTopPos();
}
