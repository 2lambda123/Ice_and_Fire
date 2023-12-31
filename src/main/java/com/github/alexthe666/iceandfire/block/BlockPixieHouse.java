package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityPixieHouse;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockPixieHouse extends BlockContainer {
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public Item itemBlock;

	public BlockPixieHouse() {
		super(Material.WOOD);
		this.setHardness(2.0F);
		this.setResistance(5.0F);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		this.setSoundType(SoundType.WOOD);
		this.setCreativeTab(IceAndFire.TAB);
		this.setUnlocalizedName("iceandfire.pixie_house");
		this.setRegistryName(IceAndFire.MODID, "pixie_house");
		GameRegistry.register(this);
		GameRegistry.registerTileEntity(TileEntityPixieHouse.class, "pixie_house");
		GameRegistry.register(itemBlock = (new ItemBlockPixieHouse(this).setRegistryName(this.getRegistryName())));
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isOpaqueCube(IBlockState blockstate) {
		return false;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isFullCube(IBlockState blockstate) {
		return false;
	}

	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		IBlockState iblockstate = worldIn.getBlockState(pos.down());
		return iblockstate.isSideSolid(worldIn, pos, EnumFacing.UP);
	}

	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		dropPixie(worldIn, pos);
		int meta = 0;
		if (worldIn.getTileEntity(pos) != null && worldIn.getTileEntity(pos) instanceof TileEntityPixieHouse) {
			meta = ((TileEntityPixieHouse) worldIn.getTileEntity(pos)).houseType;
		}
		spawnAsEntity(worldIn, pos, new ItemStack(ModBlocks.pixieHouse, 1, meta));
		super.breakBlock(worldIn, pos, state);
	}

	public int quantityDropped(Random random) {
		return 0;
	}

	@SuppressWarnings("deprecation")
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
	}

	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		this.checkFall(worldIn, pos);
	}

	private boolean checkFall(World worldIn, BlockPos pos) {
		if (!this.canPlaceBlockAt(worldIn, pos)) {
			worldIn.destroyBlock(pos, true);
			dropPixie(worldIn, pos);
			return false;
		} else {
			return true;
		}
	}

	public void dropPixie(World world, BlockPos pos) {
		if (world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntityPixieHouse && ((TileEntityPixieHouse) world.getTileEntity(pos)).hasPixie) {
			((TileEntityPixieHouse) world.getTileEntity(pos)).releasePixie();
		}
	}

	@SuppressWarnings("deprecation")
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{FACING});
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntityPixieHouse) {
			((TileEntityPixieHouse) world.getTileEntity(pos)).houseType = stack.getMetadata();
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityPixieHouse();
	}

	class ItemBlockPixieHouse extends ItemBlock {
		public ItemBlockPixieHouse(Block block) {
			super(block);
			this.maxStackSize = 1;
		}

		public String getUnlocalizedName(ItemStack stack) {
			int i = stack.getMetadata();
			return "tile.iceandfire.pixie_house_" + i;
		}

		@SideOnly(Side.CLIENT)
		public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
			for (int i = 0; i < 6; i++) {
				subItems.add(new ItemStack(itemIn, 1, i));
			}
		}
	}
}
