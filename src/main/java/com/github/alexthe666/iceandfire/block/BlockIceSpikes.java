package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class BlockIceSpikes extends Block {
	protected static final AxisAlignedBB AABB = new AxisAlignedBB(0.0625, 0.0D, 0.0625D, 0.9375D, 0.6875, 0.9375D);
	public Item itemBlock;

	public BlockIceSpikes() {
		super(Material.PACKED_ICE);
		this.setHardness(5F);
		this.setUnlocalizedName("iceandfire.dragon_ice_spikes");
		this.setCreativeTab(IceAndFire.TAB);
		this.setSoundType(SoundType.GLASS);
		this.setRegistryName(IceAndFire.MODID, "dragon_ice_spikes");
		GameRegistry.register(this);
		GameRegistry.register(itemBlock = (new ItemBlock(this).setRegistryName(this.getRegistryName())));
	}

	public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
		entityIn.attackEntityFrom(DamageSource.IN_WALL, 1);
		if (entityIn instanceof EntityLivingBase && entityIn.motionX != 0 && entityIn.motionZ != 0) {
			((EntityLivingBase) entityIn).knockBack(entityIn, 0.5F, entityIn.motionX, entityIn.motionZ);
		}
	}

	@SuppressWarnings("deprecation")
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return AABB;
	}

	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		return AABB;
	}

	@SuppressWarnings("deprecation")
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@SuppressWarnings("deprecation")
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}
}
