package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemDragonArmor extends Item {

    public int type;
    public int dragonSlot;
    public String name;

    public ItemDragonArmor(int type, int dragonSlot) {
        super(new Item.Properties().tab(IceAndFire.TAB_ITEMS).stacksTo(1));
        this.type = type;
        this.dragonSlot = dragonSlot;
    }

    Pattern baseName = Pattern.compile("[a-z]+_[a-z]+");

    @Override
    public @NotNull String getDescriptionId() {
        String fullName = this.getRegistryName().getPath();
        Matcher matcher = baseName.matcher(fullName);
        name = matcher.find() ? matcher.group() : fullName;
        return "item.iceandfire." + name;
    }

    static String getNameForSlot(int slot){
        return switch (slot) {
            case 0 -> "head";
            case 1 -> "neck";
            case 2 -> "body";
            case 3 -> "tail";
            default -> "";
        };
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        String words = switch (dragonSlot) {
            case 1 -> "dragon.armor_neck";
            case 2 -> "dragon.armor_body";
            case 3 -> "dragon.armor_tail";
            default -> "dragon.armor_head";
        };
        tooltip.add(new TranslatableComponent(words).withStyle(ChatFormatting.GRAY));
    }
}
