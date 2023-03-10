package io.github.thebusybiscuit.exoticgarden.items;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;

public class ExoticGardenFruit extends SimpleSlimefunItem<ItemUseHandler> {

    private final boolean edible;

    @ParametersAreNonnullByDefault
    public ExoticGardenFruit(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, boolean edible, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        this.edible = edible;
    }

    @ParametersAreNonnullByDefault
    public ExoticGardenFruit(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, boolean edible, ItemStack[] recipe, ItemStack recipeOutput) {
        super(itemGroup, item, recipeType, recipe, recipeOutput);
        this.edible = edible;
    }

    @Override
    public boolean useVanillaBlockBreaking() {
        return true;
    }

    @Override
    public ItemUseHandler getItemHandler() {
        return e -> {
            Optional<Block> block = e.getClickedBlock();

            if (block.isPresent()) {
                // Cancel the Block placement if the Player sneaks
                if (e.getPlayer().isSneaking()) {
                    e.cancel();
                    return;
                }

                Material material = block.get().getType();

                // Jukebox needs special handling for this reason:
                // https://github.com/SlimefunGuguProject/ExoticGarden/issues/21
                if (material == Material.JUKEBOX) {
                    Jukebox jukebox = (Jukebox) block.get().getState();
                    if (jukebox.getPlaying() == Material.AIR) {
                        e.cancel();
                    }
                    return;
                }

                // Cancel the Block placement if the Block is not interactable
                if (!isInteractable(material)) {
                    e.cancel();
                }
            }

            if (edible && e.getPlayer().getFoodLevel() < 20) {
                restoreHunger(e.getPlayer());
                ItemUtils.consumeItem(e.getItem(), false);
            }
        };
    }

    private boolean isInteractable(@Nonnull Material material) {
        // We cannot rely on Material#isInteractable() sadly
        // as it would allow the placement of this block on strange items like stairs...
        return switch (material) {
            case CRAFTING_TABLE,
                FURNACE,
                SMOKER,
                BLAST_FURNACE,
                JIGSAW,
                ENCHANTING_TABLE,
                ITEM_FRAME,
                LOOM,
                CARTOGRAPHY_TABLE,
                GRINDSTONE,
                SMITHING_TABLE,
                BELL,
                BEACON,
                ANVIL,
                BREWING_STAND,
                CAKE,
                CHEST,
                TRAPPED_CHEST,
                HOPPER,
                ENDER_CHEST -> true;
            default -> material.name().equals("BARREL") || material.name().endsWith("_SHULKER_BOX");
        };
    }

    protected int getFoodValue() {
        return 2;
    }

    private void restoreHunger(@Nonnull Player p) {
        int level = p.getFoodLevel() + getFoodValue();
        p.playSound(p.getEyeLocation(), Sound.ENTITY_GENERIC_EAT, 1, 1);
        p.setFoodLevel(Math.min(level, 20));
        p.setSaturation(p.getSaturation() + getFoodValue());
    }

}
