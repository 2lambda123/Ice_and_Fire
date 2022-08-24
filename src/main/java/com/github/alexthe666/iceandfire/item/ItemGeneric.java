package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemGeneric extends Item {
    int description = 0;

    public ItemGeneric(String name) {
        super(new Item.Properties().tab(IceAndFire.TAB_ITEMS));
        this.setRegistryName(IceAndFire.MODID, name);
    }

    public ItemGeneric(String name, int textLength) {
        super(new Item.Properties().tab(IceAndFire.TAB_ITEMS));
        this.setRegistryName(IceAndFire.MODID, name);
        this.description = textLength;
    }

    public ItemGeneric(String name, int textLength, boolean hide) {
        super(new Item.Properties());
        this.setRegistryName(IceAndFire.MODID, name);
        this.description = textLength;
    }

    public ItemGeneric(String name, int textLength, int stacksize) {
        super(new Item.Properties().tab(IceAndFire.TAB_ITEMS).stacksTo(1));
        this.setRegistryName(IceAndFire.MODID, name);
        this.description = textLength;
    }

    public boolean isFoil(ItemStack stack) {
        if (this == IafItemRegistry.CREATIVE_DRAGON_MEAL) {
            return true;
        } else {
            return super.isFoil(stack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (description > 0) {
            for (int i = 0; i < description; i++) {
                tooltip.add(new TranslationTextComponent(this.getDescriptionId() + ".desc_" + i).withStyle(TextFormatting.GRAY));
            }
        }
    }
}
