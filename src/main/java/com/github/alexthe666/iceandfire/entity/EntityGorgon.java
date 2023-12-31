package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.core.ModAchievements;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.core.ModSounds;
import com.github.alexthe666.iceandfire.entity.ai.GorgonAIStareAttack;
import com.github.alexthe666.iceandfire.message.MessageStoneStatue;
import com.google.common.base.Predicate;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityGorgon extends EntityMob implements IAnimatedEntity {

	public static Animation ANIMATION_SCARE;
	public static Animation ANIMATION_HIT;
	private int animationTick;
	private Animation currentAnimation;
	private GorgonAIStareAttack aiStare;
	private EntityAIAttackMelee aiMelee;

	public EntityGorgon(World worldIn) {
		super(worldIn);
		this.setSize(0.8F, 1.99F);
		ANIMATION_SCARE = Animation.create(30);
		ANIMATION_HIT = Animation.create(10);
	}

	public static boolean isEntityLookingAt(EntityLivingBase looker, EntityLivingBase seen, double degree) {
		Vec3d vec3d = looker.getLook(1.0F).normalize();
		Vec3d vec3d1 = new Vec3d(seen.posX - looker.posX, seen.getEntityBoundingBox().minY + (double) seen.getEyeHeight() - (looker.posY + (double) looker.getEyeHeight()), seen.posZ - looker.posZ);
		double d0 = vec3d1.lengthVector();
		vec3d1 = vec3d1.normalize();
		double d1 = vec3d.dotProduct(vec3d1);
		return d1 > 1.0D - degree / d0 ? looker.canEntityBeSeen(seen) && !isStoneMob(seen) : false;
	}

	public static boolean isStoneMob(EntityLivingBase mob) {
		if (mob != null && mob instanceof EntityLiving) {
			StoneEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(mob, StoneEntityProperties.class);
			return properties != null && properties.isStone;
		}
		return false;
	}

	protected void initEntityAI() {
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAIRestrictSun(this));
		this.tasks.addTask(3, new EntityAIFleeSun(this, 1.0D));
		this.tasks.addTask(3, aiStare = new GorgonAIStareAttack(this, 1.0D, 0, 15.0F));
		this.tasks.addTask(3, aiMelee = new EntityAIAttackMelee(this, 1.0D, false));
		this.tasks.addTask(4, new GorgonAIStareAttack(this, 1.0D, 0, 15.0F));
		this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D) {
			public boolean shouldExecute() {
				executionChance = 20;
				return super.shouldExecute();
			}
		});
		this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F, 1.0F) {
			public boolean shouldContinueExecuting() {
				if (this.closestEntity != null && this.closestEntity instanceof EntityPlayer && ((EntityPlayer) this.closestEntity).isCreative()) {
					return false;
				}
				return super.shouldContinueExecuting();
			}
		});
		this.tasks.addTask(6, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true, false, new Predicate<EntityPlayer>() {
			@Override
			public boolean apply(@Nullable EntityPlayer entity) {
				return true;
			}
		}));
		this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityLiving.class, 0, true, false, new Predicate<Entity>() {
			@Override
			public boolean apply(@Nullable Entity entity) {
				StoneEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(entity, StoneEntityProperties.class);
				return entity instanceof EntityLiving && !(entity instanceof IBlacklistedFromStatues) || (properties == null || properties != null && !properties.isStone);
			}
		}));
		this.tasks.removeTask(aiMelee);
	}

	public void attackEntityWithRangedAttack(EntityLivingBase entity) {
		if (!(entity instanceof EntityPlayer) && entity instanceof EntityLiving) {
			forcePreyToLook((EntityLiving) entity);
		}
	}

	public boolean attackEntityAsMob(Entity entityIn) {
		boolean blindness = this.isPotionActive(MobEffects.BLINDNESS) || (entityIn instanceof EntityLivingBase && ((EntityLivingBase) entityIn).isPotionActive(MobEffects.BLINDNESS));
		if (blindness) {
			if (this.getAnimation() != ANIMATION_HIT) {
				this.setAnimation(ANIMATION_HIT);
			}
			if (entityIn instanceof EntityLivingBase) {
				((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MobEffects.POISON, 100, 2, false, true));
			}
		}
		return super.attackEntityAsMob(entityIn);
	}

	protected boolean canDespawn() {
		return false;
	}

	public void setAttackTarget(@Nullable EntityLivingBase entitylivingbaseIn) {
		super.setAttackTarget(entitylivingbaseIn);
		if (entitylivingbaseIn != null) {
			boolean blindness = this.isPotionActive(MobEffects.BLINDNESS) || entitylivingbaseIn.isPotionActive(MobEffects.BLINDNESS);
			if (blindness) {
				this.tasks.removeTask(aiStare);
				this.tasks.addTask(3, aiMelee);
			} else {
				this.tasks.removeTask(aiMelee);
				this.tasks.addTask(3, aiStare);
			}
		}
	}

	protected void onDeathUpdate() {
		if (deathTime == 20 && !world.isRemote) {
			this.entityDropItem(new ItemStack(ModItems.gorgon_head), 0);
		}
		++this.deathTime;
		this.livingSoundTime = 20;
		for (int k = 0; k < 5; ++k) {
			double d2 = 0.4;
			double d0 = 0.1;
			double d1 = 0.1;
			IceAndFire.PROXY.spawnParticle("blood", this.world, this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY, this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, d2, d0, d1);
		}
		if (this.deathTime >= 200) {
			if (!this.world.isRemote && (this.isPlayer() || this.recentlyHit > 0 && this.canDropLoot() && this.world.getGameRules().getBoolean("doMobLoot"))) {
				int i = this.getExperiencePoints(this.attackingPlayer);
				i = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(this, this.attackingPlayer, i);
				while (i > 0) {
					int j = EntityXPOrb.getXPSplit(i);
					i -= j;
					this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY, this.posZ, j));
				}
			}
			this.setDead();

			for (int k = 0; k < 20; ++k) {
				double d2 = this.rand.nextGaussian() * 0.02D;
				double d0 = this.rand.nextGaussian() * 0.02D;
				double d1 = this.rand.nextGaussian() * 0.02D;
				this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, this.posX + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, this.posY + (double) (this.rand.nextFloat() * this.height), this.posZ + (double) (this.rand.nextFloat() * this.width * 2.0F) - (double) this.width, d2, d0, d1, new int[0]);
			}
		}
	}

	public void onDeath(DamageSource cause) {
		if (cause.getTrueSource() instanceof EntityPlayer) {
			((EntityPlayer) cause.getTrueSource()).addStat(ModAchievements.killGorgon);
		}
	}

	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (this.getAttackTarget() != null) {
			boolean blindness = this.isPotionActive(MobEffects.BLINDNESS) || this.getAttackTarget().isPotionActive(MobEffects.BLINDNESS);
			this.getLookHelper().setLookPosition(this.getAttackTarget().posX, this.getAttackTarget().posY + (double) this.getAttackTarget().getEyeHeight(), this.getAttackTarget().posZ, (float) this.getHorizontalFaceSpeed(), (float) this.getVerticalFaceSpeed());
			if (!blindness && this.getAttackTarget() instanceof EntityLiving && !(this.getAttackTarget() instanceof EntityPlayer)) {
				forcePreyToLook((EntityLiving) this.getAttackTarget());
			}
		}

		if (this.getAttackTarget() != null && isEntityLookingAt(this, this.getAttackTarget(), 0.4) && isEntityLookingAt(this.getAttackTarget(), this, 0.4)) {
			boolean blindness = this.isPotionActive(MobEffects.BLINDNESS) || this.getAttackTarget().isPotionActive(MobEffects.BLINDNESS);
			if (!blindness) {
				if (this.getAnimation() != ANIMATION_SCARE) {
					this.playSound(ModSounds.gorgon_attack, 1, 1);
					this.setAnimation(ANIMATION_SCARE);
				}
				if (this.getAnimation() == ANIMATION_SCARE) {
					if (this.getAnimationTick() > 10) {
						if (this.getAttackTarget() instanceof EntityPlayer) {
							EntityStoneStatue statue = new EntityStoneStatue(world);
							statue.setPositionAndRotation(this.getAttackTarget().posX, this.getAttackTarget().posY, this.getAttackTarget().posZ, this.getAttackTarget().rotationYaw, this.getAttackTarget().rotationPitch);
							statue.smallArms = true;
							if (!world.isRemote) {
								world.spawnEntity(statue);
							}
							statue.prevRotationYaw = this.getAttackTarget().rotationYaw;
							statue.rotationYaw = this.getAttackTarget().rotationYaw;
							statue.rotationYawHead = this.getAttackTarget().rotationYaw;
							statue.renderYawOffset = this.getAttackTarget().rotationYaw;
							statue.prevRenderYawOffset = this.getAttackTarget().rotationYaw;
							this.getAttackTarget().attackEntityFrom(IceAndFire.gorgon, Integer.MAX_VALUE);
						} else {
							if (this.getAttackTarget() instanceof EntityLiving) {
								StoneEntityProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(this.getAttackTarget(), StoneEntityProperties.class);
								EntityLiving attackTarget = (EntityLiving) this.getAttackTarget();
								if (properties != null || !properties.isStone) {
									properties.isStone = true;
									if (world.isRemote) {
										IceAndFire.NETWORK_WRAPPER.sendToServer(new MessageStoneStatue(attackTarget.getEntityId(), true));
									} else {
										IceAndFire.NETWORK_WRAPPER.sendToAll(new MessageStoneStatue(attackTarget.getEntityId(), true));
									}
									this.playSound(ModSounds.gorgon_turn_stone, 1, 1);
									this.setAttackTarget(null);
								}

								if (attackTarget instanceof EntityDragonBase) {
									EntityDragonBase dragon = (EntityDragonBase) attackTarget;
									dragon.setFlying(false);
									dragon.setHovering(false);
									dragon.airTarget = null;
								}
								if (attackTarget instanceof EntityHippogryph) {
									EntityHippogryph dragon = (EntityHippogryph) attackTarget;
									dragon.setFlying(false);
									dragon.setHovering(false);
									dragon.airTarget = null;
								}
							} else {
								this.getAttackTarget().setDead();
							}
						}
					}
				}
			}
		}
		AnimationHandler.INSTANCE.updateAnimations(this);
		EntityPlayer player = world.getClosestPlayerToEntity(this, 25);
		if (player != null) {
			player.addStat(ModAchievements.findGorgon);
		}
	}

	public int getVerticalFaceSpeed() {
		return 10;
	}

	public int getHorizontalFaceSpeed() {
		return 10;
	}

	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.UNDEAD;
	}

	public void forcePreyToLook(EntityLiving mob) {
		mob.getLookHelper().setLookPosition(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ, (float) mob.getHorizontalFaceSpeed(), (float) mob.getVerticalFaceSpeed());
	}

	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(IceAndFire.CONFIG.gorgonMaxHealth);
	}

	@Override
	public int getAnimationTick() {
		return animationTick;
	}

	@Override
	public void setAnimationTick(int tick) {
		animationTick = tick;
	}

	@Override
	public Animation getAnimation() {
		return currentAnimation;
	}

	@Override
	public void setAnimation(Animation animation) {
		currentAnimation = animation;
	}

	@Override
	public Animation[] getAnimations() {
		return new Animation[]{ANIMATION_SCARE, ANIMATION_HIT};
	}

	@Nullable
	protected SoundEvent getAmbientSound() {
		return ModSounds.gorgon_idle;
	}

	@Nullable
	protected SoundEvent getHurtSound() {
		return ModSounds.gorgon_hurt;
	}

	@Nullable
	protected SoundEvent getDeathSound() {
		return ModSounds.gorgon_die;
	}
}
