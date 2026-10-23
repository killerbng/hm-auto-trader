package dev.hm.autotrader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;

/**
 * The network and merchant selection code lives here to simplify 26.3 porting.
 * No server mod, custom packets, reflection, or MaLiLib required.
 *
 * One click per delay; the server is authoritative for every inventory transaction.
 */
public final class TraderController {
    private enum State { IDLE, WAIT_SCREEN, PICK_OFFER, WAIT_RESULT, COOL_CLICK }
    private State state = State.IDLE;
    private UUID target;
    private int ticks;
    private int wait;
    private int offerCursor;
    private int activeOffer = -1;
    private int tradesWithVillager;
    private final Set<UUID> visited = new HashSet<>();
    private final List<Integer> indices = new ArrayList<>();

    public void reset() {
        state = State.IDLE;
        target = null;
        ticks = 0;
        wait = 0;
        offerCursor = 0;
        activeOffer = -1;
        tradesWithVillager = 0;
        visited.clear();
        indices.clear();
    }

    public void tick(Minecraft mc) {
        Settings config = HMAutoTraderClient.SETTINGS;
        if (!config.enabled || mc.player == null || mc.gameMode == null || mc.level == null) {
            if (state != State.IDLE) reset();
            return;
        }
        if (HMAutoTraderClient.FAVORITES.size() == 0) {
            config.enabled = false;
            HMAutoTraderClient.status("No favorites saved. Shift + middle-click merchant trade rows.");
            return;
        }
        if (wait > 0) { wait--; return; }

        if (target == null) {
            if (mc.gui.screen() != null) return; // Never control other GUIs.
            findAndOpen(mc, config);
            return;
        }

        if (state == State.WAIT_SCREEN) {
            ticks++;
            if (mc.gui.screen() instanceof MerchantScreen screen && !screen.getMenu().getOffers().isEmpty()) {
                indices.clear();
                MerchantMenu menu = screen.getMenu();
                for (int i = 0; i < menu.getOffers().size(); i++) {
                    MerchantOffer offer = menu.getOffers().get(i);
                    if (HMAutoTraderClient.FAVORITES.contains(offer)) indices.add(i);
                }
                offerCursor = 0;
                tradesWithVillager = 0;
                state = State.PICK_OFFER;
                ticks = 0;
                if (indices.isEmpty()) HMAutoTraderClient.status("No matching favorites for villager");
                return;
            }
            if (ticks > 60) finish(mc, config);
            return;
        }

        if (!(mc.gui.screen() instanceof MerchantScreen screen)) {
            // If a server / player closes the trading UI unexpectedly, abandon the target.
            target = null;
            state = State.IDLE;
            wait = config.villagerDelayTicks;
            return;
        }
        MerchantMenu menu = screen.getMenu();
        if (state == State.PICK_OFFER) {
            if (offerCursor >= indices.size() || tradesWithVillager >= 256) {
                finish(mc, config);
                return;
            }
            activeOffer = indices.get(offerCursor);
            if (activeOffer >= menu.getOffers().size() || menu.getOffers().get(activeOffer).isOutOfStock()) {
                nextOffer(); return;
            }
            // Don't initiate a transaction that could spill items on the ground.
            if (config.stopIfFull && !canFit(mc.player, menu.getOffers().get(activeOffer).getResult())) {
                config.enabled = false;
                HMAutoTraderClient.status("Stopped: not enough inventory room");
                return;
            }
            // Replicates vanilla MerchantScreen.postButtonClick(): select and autofill.
            menu.setSelectionHint(activeOffer);
            menu.tryMoveItems(activeOffer);
            if (mc.getConnection() != null) mc.getConnection().send(new ServerboundSelectTradePacket(activeOffer));
            state = State.WAIT_RESULT;
            ticks = 0;
            wait = config.clickDelayTicks;
            return;
        }
        if (state == State.WAIT_RESULT) {
            ticks++;
            if (activeOffer >= menu.getOffers().size() || menu.getOffers().get(activeOffer).isOutOfStock()) {
                nextOffer(); return;
            }
            ItemStack output = menu.getSlot(2).getItem();
            if (!output.isEmpty() && ItemStack.isSameItem(output, menu.getOffers().get(activeOffer).getResult())) {
                if (config.stopIfFull && !canFit(mc.player, output)) {
                    config.enabled = false;
                    HMAutoTraderClient.status("Stopped: inventory full");
                    return;
                }
                // Vanilla shift-click on merchant RESULT SLOT (index 2).
                mc.gameMode.handleContainerInput(menu.containerId, 2, 0, ContainerInput.QUICK_MOVE, mc.player);
                tradesWithVillager++;
                ticks = 0;
                state = State.COOL_CLICK;
                wait = config.clickDelayTicks;
                return;
            }
            // Avoid hanging forever on exhausted trades or insufficient input.
            if (ticks > 15) nextOffer();
            return;
        }
        if (state == State.COOL_CLICK) {
            if (activeOffer >= menu.getOffers().size() || menu.getOffers().get(activeOffer).isOutOfStock()) {
                nextOffer(); return;
            }
            // Some trades can produce the next result without another select; otherwise
            // vanilla's autofill will run again when we select the same offer.
            if (!menu.getSlot(2).getItem().isEmpty()) {
                state = State.WAIT_RESULT;
                ticks = 0;
            } else {
                state = State.PICK_OFFER;
            }
        }
    }

    private void findAndOpen(Minecraft mc, Settings config) {
        Player player = mc.player;
        if (player == null) return;
        AABB area = player.getBoundingBox().inflate(config.range);
        List<Villager> nearby = mc.level.getEntitiesOfClass(Villager.class, area,
                v -> v.isAlive() && v.distanceTo(player) <= config.range && professionAllowed(v, config));
        nearby.sort(Comparator.comparingDouble(v -> v.distanceToSqr(player)));
        for (Villager villager : nearby) {
            if (visited.contains(villager.getUUID())) continue;
            visited.add(villager.getUUID());
            target = villager.getUUID();
            state = State.WAIT_SCREEN;
            ticks = 0;
            mc.gameMode.interact(player, villager, new EntityHitResult(villager), InteractionHand.MAIN_HAND);
            return;
        }
        // Completed one pass of all reachable villagers. Rest before rescanning.
        if (!visited.isEmpty()) {
            visited.clear();
            wait = config.roundDelayTicks;
            HMAutoTraderClient.status("Round complete; resting " + (config.roundDelayTicks / 20) + "s");
        } else wait = 20;
    }

    private boolean professionAllowed(Villager v, Settings config) {
        if ("all".equals(config.profession)) return true;
        var id = BuiltInRegistries.VILLAGER_PROFESSION.getKey(v.getVillagerData().profession().value());
        return id != null && id.toString().equals(config.profession);
    }

    private void nextOffer() {
        offerCursor++;
        activeOffer = -1;
        state = State.PICK_OFFER;
        ticks = 0;
    }

    private void finish(Minecraft mc, Settings config) {
        HMAutoTraderClient.status("Finished villager: " + tradesWithVillager + " trades");
        if (config.closeAfterVillager && mc.player != null && mc.gui.screen() instanceof MerchantScreen) {
            mc.player.closeContainer();
        }
        target = null;
        state = State.IDLE;
        activeOffer = -1;
        indices.clear();
        wait = config.villagerDelayTicks;
    }

    private boolean canFit(Player player, ItemStack result) {
        var inventory = player.getInventory();
        for (int i = 0; i < 36; i++) {
            ItemStack existing = inventory.getItem(i);
            if (existing.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(existing, result)
                    && existing.getCount() + result.getCount() <= existing.getMaxStackSize()) return true;
        }
        return false;
    }
}
