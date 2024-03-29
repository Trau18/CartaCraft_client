package net.minecraft.src;

import java.util.HashMap;
import java.util.Map;

public class FurnaceRecipes
{
    private static final FurnaceRecipes smeltingBase = new FurnaceRecipes();

    /** The list of smelting results. */
    private Map smeltingList = new HashMap();

    /**
     * Used to call methods addSmelting and getSmeltingResult.
     */
    public static final FurnaceRecipes smelting()
    {
        return smeltingBase;
    }

    private FurnaceRecipes()
    {
        this.addSmelting(Block.oreIron.blockID, new ItemStack(Item.ingotIron));
        this.addSmelting(Block.oreGold.blockID, new ItemStack(Item.ingotGold));
        this.addSmelting(Block.oreDiamond.blockID, new ItemStack(Item.diamond));
        this.addSmelting(Block.sand.blockID, new ItemStack(Block.glass));
        this.addSmelting(Item.porkRaw.shiftedIndex, new ItemStack(Item.porkCooked));
        this.addSmelting(Item.beefRaw.shiftedIndex, new ItemStack(Item.beefCooked));
        this.addSmelting(Item.chickenRaw.shiftedIndex, new ItemStack(Item.chickenCooked));
        this.addSmelting(Item.fishRaw.shiftedIndex, new ItemStack(Item.fishCooked));
        this.addSmelting(Block.cobblestone.blockID, new ItemStack(Block.stone));
        this.addSmelting(Item.clay.shiftedIndex, new ItemStack(Item.brick));
        this.addSmelting(Block.cactus.blockID, new ItemStack(Item.dyePowder, 1, 2));
        this.addSmelting(Block.wood.blockID, new ItemStack(Item.coal, 1, 1));
        this.addSmelting(Block.oreCoal.blockID, new ItemStack(Item.coal));
        this.addSmelting(Block.oreRedstone.blockID, new ItemStack(Item.redstone));
        this.addSmelting(Block.oreLapis.blockID, new ItemStack(Item.dyePowder, 1, 4));
    }

    /**
     * Adds a smelting recipe.
     */
    public void addSmelting(int par1, ItemStack par2ItemStack)
    {
        this.smeltingList.put(Integer.valueOf(par1), par2ItemStack);
    }

    /**
     * Returns the smelting result of an item.
     */
    public ItemStack getSmeltingResult(int par1)
    {
        return (ItemStack)this.smeltingList.get(Integer.valueOf(par1));
    }

    public Map getSmeltingList()
    {
        return this.smeltingList;
    }
}
