package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.StatCollector;
import com.github.alexthe666.iceandfire.entity.EntityDragonSkull;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemDragonSkull extends Item {

	public ItemDragonSkull() {
		this.maxStackSize = 1;
		this.setCreativeTab(IceAndFire.TAB);
		this.setUnlocalizedName("iceandfire.dragon_skull");
		this.setRegistryName(IceAndFire.MODID, "dragon_skull");
		GameRegistry.register(this);
	}

	@Override
	public void onCreated(ItemStack itemStack, World world, EntityPlayer player) {
		itemStack.setTagCompound(new NBTTagCompound());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		subItems.add(new ItemStack(itemIn, 1, 0));
		subItems.add(new ItemStack(itemIn, 1, 1));
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int f, boolean f1) {
		if (stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setInteger("Stage", 4);
			stack.getTagCompound().setInteger("DragonAge", 75);

		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean f) {
		String iceorfire = stack.getMetadata() == 0 ? "dragon.fire" : "dragon.ice";
		list.add(StatCollector.translateToLocal(iceorfire));
		if (stack.getTagCompound() != null) {
			list.add(StatCollector.translateToLocal("dragon.stage") + stack.getTagCompound().getInteger("Stage"));
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (side != EnumFacing.UP) {
			return EnumActionResult.FAIL;
		} else {
			ItemStack stack = player.getHeldItem(hand);
			/*
             * EntityDragonEgg egg = new EntityDragonEgg(worldIn);
			 * egg.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() +
			 * 0.5); if(!worldIn.isRemote){ worldIn.spawnEntityInWorld(egg); }
			 */
			if (stack.getTagCompound() != null) {

				EntityDragonSkull skull = new EntityDragonSkull(worldIn);
				skull.setType(stack.getMetadata());
				skull.setStage(stack.getTagCompound().getInteger("Stage"));
				skull.setDragonAge(stack.getTagCompound().getInteger("DragonAge"));
				skull.setLocationAndAngles(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0);
				skull.rotationYaw = player.rotationYaw;

				if (!worldIn.isRemote) {
					worldIn.spawnEntity(skull);
				}
				if (!player.capabilities.isCreativeMode) {
					stack.shrink(1);

					if (stack.isEmpty()) {
						player.inventory.setInventorySlotContents(player.inventory.currentItem, (ItemStack.EMPTY));
					} else {
						player.inventory.setInventorySlotContents(player.inventory.currentItem, stack);
					}
				}
			}
			return EnumActionResult.SUCCESS;

		}
	}

	/*
     * @Override public ModelResourceLocation getModel(ItemStack stack,
	 * EntityPlayer player, int useRemaining) { switch(stack.getMetadata()){
	 * default: return new ModelResourceLocation("iceandfire:dragon_skull_fire",
	 * "inventory"); case 1: return new
	 * ModelResourceLocation("iceandfire:dragon_skull_ice", "inventory"); } }
	 */
}
