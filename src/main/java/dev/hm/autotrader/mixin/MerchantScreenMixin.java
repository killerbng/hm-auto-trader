package dev.hm.autotrader.mixin;

import dev.hm.autotrader.HMAutoTraderClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Shift + middle click a visible merchant OFFER ROW: global favorite toggle. */
@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin {
    @Shadow private int scrollOff;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void hm$favoriteOffer(MouseButtonEvent click, boolean doubleClick,
                                   CallbackInfoReturnable<Boolean> cir) {
        if (click.button() != 2 || !Minecraft.getInstance().hasShiftDown()) return;
        MerchantScreen screen = (MerchantScreen) (Object) this;
        // Vanilla trade list: x=left+5..93, y=top+18..158, seven rows of 20.
        int rx = (int) click.x() - ((AbstractContainerScreenAccessor) (Object) screen).hm$getLeftPos();
        int ry = (int) click.y() - ((AbstractContainerScreenAccessor) (Object) screen).hm$getTopPos();
        if (rx < 5 || rx >= 93 || ry < 18 || ry >= 158) return;
        int index = (ry - 18) / 20 + scrollOff;
        MerchantMenu menu = screen.getMenu();
        if (index < 0 || index >= menu.getOffers().size()) return;
        boolean nowFavorite = HMAutoTraderClient.FAVORITES.toggle(menu.getOffers().get(index));
        HMAutoTraderClient.status(nowFavorite ? "★ Favorited trade" : "☆ Unfavorited trade");
        cir.setReturnValue(true);
    }
}
