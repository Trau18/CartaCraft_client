package net.minecraft.src;

import java.util.Iterator;
import java.util.List;

public abstract class EntityPlayer extends EntityLiving
{
    /** Inventory of the player */
    public InventoryPlayer inventory = new InventoryPlayer(this);

    /** the crafting inventory in you get when opening your inventory */
    public Container inventorySlots;

    /** the crafting inventory you are currently using */
    public Container craftingInventory;

    /** The player's food stats. (See class FoodStats) */
    protected FoodStats foodStats = new FoodStats();

    /**
     * Used to tell if the player pressed jump twice. If this is at 0 and it's pressed (And they are allowed to fly, as
     * defined in the player's movementInput) it sets this to 7. If it's pressed and it's greater than 0 enable fly.
     */
    protected int flyToggleTimer = 0;
    public byte field_9371_f = 0;
    public int score = 0;
    public float prevCameraYaw;
    public float cameraYaw;

    /** Whether the player is swinging the current item in their hand. */
    public boolean isSwinging = false;
    public int swingProgressInt = 0;
    public String username;

    /** Which dimension the player is in (-1 = the Nether, 0 = normal world) */
    public int dimension;
    public String playerCloakUrl;

    /**
     * Used by EntityPlayer to prevent too many xp orbs from getting absorbed at once.
     */
    public int xpCooldown = 0;
    public double field_20066_r;
    public double field_20065_s;
    public double field_20064_t;
    public double field_20063_u;
    public double field_20062_v;
    public double field_20061_w;

    /** Boolean value indicating weather a player is sleeping or not */
    protected boolean sleeping;

    /**
     * The chunk coordinates of the bed the player is in (null if player isn't in a bed).
     */
    public ChunkCoordinates playerLocation;
    private int sleepTimer;
    public float field_22063_x;
    public float field_22062_y;
    public float field_22061_z;

    /**
     * Holds the last coordinate to spawn based on last bed that the player sleep.
     */
    private ChunkCoordinates spawnChunk;

    /** Holds the coordinate of the player when enter a minecraft to ride. */
    private ChunkCoordinates startMinecartRidingCoordinate;
    public int timeUntilPortal = 20;

    /** Whether the entity is inside a Portal */
    protected boolean inPortal = false;

    /** The amount of time an entity has been in a Portal */
    public float timeInPortal;

    /** The amount of time an entity has been in a Portal the previous tick */
    public float prevTimeInPortal;

    /** The player's capabilities. (See class PlayerCapabilities) */
    public PlayerCapabilities capabilities = new PlayerCapabilities();

    /** The current experience level the player is on. */
    public int experienceLevel;

    /**
     * The total amount of experience the player has. This also includes the amount of experience within their
     * Experience Bar.
     */
    public int experienceTotal;

    /**
     * The current amount of experience the player has within their Experience Bar.
     */
    public float experience;

    /**
     * This is the item that is in use when the player is holding down the useItemButton (e.g., bow, food, sword)
     */
    private ItemStack itemInUse;

    /**
     * This field starts off equal to getMaxItemUseDuration and is decremented on each tick
     */
    private int itemInUseCount;
    protected float speedOnGround = 0.1F;
    protected float speedInAir = 0.02F;

    /**
     * An instance of a fishing rod's hook. If this isn't null, the icon image of the fishing rod is slightly different
     */
    public EntityFishHook fishEntity = null;

    public EntityPlayer(World par1World)
    {
        super(par1World);
        this.inventorySlots = new ContainerPlayer(this.inventory, !par1World.isRemote);
        this.craftingInventory = this.inventorySlots;
        this.yOffset = 1.62F;
        ChunkCoordinates var2 = par1World.getSpawnPoint();
        this.setLocationAndAngles((double)var2.posX + 0.5D, (double)(var2.posY + 1), (double)var2.posZ + 0.5D, 0.0F, 0.0F);
        this.entityType = "humanoid";
        this.field_9353_B = 180.0F;
        this.fireResistance = 20;
        this.texture = "/mob/char.png";
    }

    public int getMaxHealth()
    {
        return 20;
    }

    protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, Byte.valueOf((byte)0));
        this.dataWatcher.addObject(17, Byte.valueOf((byte)0));
    }

    /**
     * returns the ItemStack containing the itemInUse
     */
    public ItemStack getItemInUse()
    {
        return this.itemInUse;
    }

    /**
     * Returns the item in use count
     */
    public int getItemInUseCount()
    {
        return this.itemInUseCount;
    }

    /**
     * Checks if the entity is currently using an item (e.g., bow, food, sword) by holding down the useItemButton
     */
    public boolean isUsingItem()
    {
        return this.itemInUse != null;
    }

    /**
     * gets the duration for how long the current itemInUse has been in use
     */
    public int getItemInUseDuration()
    {
        return this.isUsingItem() ? this.itemInUse.getMaxItemUseDuration() - this.itemInUseCount : 0;
    }

    public void stopUsingItem()
    {
        if (this.itemInUse != null)
        {
            this.itemInUse.onPlayerStoppedUsing(this.worldObj, this, this.itemInUseCount);
        }

        this.clearItemInUse();
    }

    public void clearItemInUse()
    {
        this.itemInUse = null;
        this.itemInUseCount = 0;

        if (!this.worldObj.isRemote)
        {
            this.setEating(false);
        }
    }

    public boolean isBlocking()
    {
        return this.isUsingItem() && Item.itemsList[this.itemInUse.itemID].getItemUseAction(this.itemInUse) == EnumAction.block;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        if (this.itemInUse != null)
        {
            ItemStack var1 = this.inventory.getCurrentItem();

            if (var1 != this.itemInUse)
            {
                this.clearItemInUse();
            }
            else
            {
                if (this.itemInUseCount <= 25 && this.itemInUseCount % 4 == 0)
                {
                    this.updateItemUse(var1, 5);
                }

                if (--this.itemInUseCount == 0 && !this.worldObj.isRemote)
                {
                    this.onItemUseFinish();
                }
            }
        }

        if (this.xpCooldown > 0)
        {
            --this.xpCooldown;
        }

        if (this.isPlayerSleeping())
        {
            ++this.sleepTimer;

            if (this.sleepTimer > 100)
            {
                this.sleepTimer = 100;
            }

            if (!this.worldObj.isRemote)
            {
                if (!this.isInBed())
                {
                    this.wakeUpPlayer(true, true, false);
                }
                else if (this.worldObj.isDaytime())
                {
                    this.wakeUpPlayer(false, true, true);
                }
            }
        }
        else if (this.sleepTimer > 0)
        {
            ++this.sleepTimer;

            if (this.sleepTimer >= 110)
            {
                this.sleepTimer = 0;
            }
        }

        super.onUpdate();

        if (!this.worldObj.isRemote && this.craftingInventory != null && !this.craftingInventory.canInteractWith(this))
        {
            this.closeScreen();
            this.craftingInventory = this.inventorySlots;
        }

        if (this.capabilities.isFlying)
        {
            for (int var9 = 0; var9 < 8; ++var9)
            {
                ;
            }
        }

        if (this.isBurning() && this.capabilities.disableDamage)
        {
            this.extinguish();
        }

        this.field_20066_r = this.field_20063_u;
        this.field_20065_s = this.field_20062_v;
        this.field_20064_t = this.field_20061_w;
        double var10 = this.posX - this.field_20063_u;
        double var3 = this.posY - this.field_20062_v;
        double var5 = this.posZ - this.field_20061_w;
        double var7 = 10.0D;

        if (var10 > var7)
        {
            this.field_20066_r = this.field_20063_u = this.posX;
        }

        if (var5 > var7)
        {
            this.field_20064_t = this.field_20061_w = this.posZ;
        }

        if (var3 > var7)
        {
            this.field_20065_s = this.field_20062_v = this.posY;
        }

        if (var10 < -var7)
        {
            this.field_20066_r = this.field_20063_u = this.posX;
        }

        if (var5 < -var7)
        {
            this.field_20064_t = this.field_20061_w = this.posZ;
        }

        if (var3 < -var7)
        {
            this.field_20065_s = this.field_20062_v = this.posY;
        }

        this.field_20063_u += var10 * 0.25D;
        this.field_20061_w += var5 * 0.25D;
        this.field_20062_v += var3 * 0.25D;
        this.addStat(StatList.minutesPlayedStat, 1);

        if (this.ridingEntity == null)
        {
            this.startMinecartRidingCoordinate = null;
        }

        if (!this.worldObj.isRemote)
        {
            this.foodStats.onUpdate(this);
        }
    }

    /**
     * Plays sounds and makes particles for item in use state
     */
    protected void updateItemUse(ItemStack par1ItemStack, int par2)
    {
        if (par1ItemStack.getItemUseAction() == EnumAction.drink)
        {
            this.worldObj.playSoundAtEntity(this, "random.drink", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
        }

        if (par1ItemStack.getItemUseAction() == EnumAction.eat)
        {
            for (int var3 = 0; var3 < par2; ++var3)
            {
                Vec3D var4 = Vec3D.createVector(((double)this.rand.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
                var4.rotateAroundX(-this.rotationPitch * (float)Math.PI / 180.0F);
                var4.rotateAroundY(-this.rotationYaw * (float)Math.PI / 180.0F);
                Vec3D var5 = Vec3D.createVector(((double)this.rand.nextFloat() - 0.5D) * 0.3D, (double)(-this.rand.nextFloat()) * 0.6D - 0.3D, 0.6D);
                var5.rotateAroundX(-this.rotationPitch * (float)Math.PI / 180.0F);
                var5.rotateAroundY(-this.rotationYaw * (float)Math.PI / 180.0F);
                var5 = var5.addVector(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ);
                this.worldObj.spawnParticle("iconcrack_" + par1ItemStack.getItem().shiftedIndex, var5.xCoord, var5.yCoord, var5.zCoord, var4.xCoord, var4.yCoord + 0.05D, var4.zCoord);
            }

            this.worldObj.playSoundAtEntity(this, "random.eat", 0.5F + 0.5F * (float)this.rand.nextInt(2), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
        }
    }

    /**
     * Used for when item use count runs out, ie: eating completed
     */
    protected void onItemUseFinish()
    {
        if (this.itemInUse != null)
        {
            this.updateItemUse(this.itemInUse, 16);
            int var1 = this.itemInUse.stackSize;
            ItemStack var2 = this.itemInUse.onFoodEaten(this.worldObj, this);

            if (var2 != this.itemInUse || var2 != null && var2.stackSize != var1)
            {
                this.inventory.mainInventory[this.inventory.currentItem] = var2;

                if (var2.stackSize == 0)
                {
                    this.inventory.mainInventory[this.inventory.currentItem] = null;
                }
            }

            this.clearItemInUse();
        }
    }

    public void handleHealthUpdate(byte par1)
    {
        if (par1 == 9)
        {
            this.onItemUseFinish();
        }
        else
        {
            super.handleHealthUpdate(par1);
        }
    }

    /**
     * Dead and sleeping entities cannot move
     */
    protected boolean isMovementBlocked()
    {
        return this.getHealth() <= 0 || this.isPlayerSleeping();
    }

    /**
     * sets current screen to null (used on escape buttons of GUIs)
     */
    protected void closeScreen()
    {
        this.craftingInventory = this.inventorySlots;
    }

    public void updateCloak()
    {
        this.playerCloakUrl = "http://s3.amazonaws.com/MinecraftCloaks/" + this.username + ".png";
        this.cloakUrl = this.playerCloakUrl;
    }

    /**
     * Handles updating while being ridden by an entity
     */
    public void updateRidden()
    {
        double var1 = this.posX;
        double var3 = this.posY;
        double var5 = this.posZ;
        super.updateRidden();
        this.prevCameraYaw = this.cameraYaw;
        this.cameraYaw = 0.0F;
        this.addMountedMovementStat(this.posX - var1, this.posY - var3, this.posZ - var5);
    }

    /**
     * Keeps moving the entity up so it isn't colliding with blocks and other requirements for this entity to be spawned
     * (only actually used on players though its also on Entity)
     */
    public void preparePlayerToSpawn()
    {
        this.yOffset = 1.62F;
        this.setSize(0.6F, 1.8F);
        super.preparePlayerToSpawn();
        this.setEntityHealth(this.getMaxHealth());
        this.deathTime = 0;
    }

    /**
     * Returns the swing speed modifier
     */
    private int getSwingSpeedModifier()
    {
        return this.isPotionActive(Potion.digSpeed) ? 6 - (1 + this.getActivePotionEffect(Potion.digSpeed).getAmplifier()) * 1 : (this.isPotionActive(Potion.digSlowdown) ? 6 + (1 + this.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2 : 6);
    }

    protected void updateEntityActionState()
    {
        int var1 = this.getSwingSpeedModifier();

        if (this.isSwinging)
        {
            ++this.swingProgressInt;

            if (this.swingProgressInt >= var1)
            {
                this.swingProgressInt = 0;
                this.isSwinging = false;
            }
        }
        else
        {
            this.swingProgressInt = 0;
        }

        this.swingProgress = (float)this.swingProgressInt / (float)var1;
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate()
    {
        if (this.flyToggleTimer > 0)
        {
            --this.flyToggleTimer;
        }

        if (this.worldObj.difficultySetting == 0 && this.getHealth() < this.getMaxHealth() && this.ticksExisted % 20 * 12 == 0)
        {
            this.heal(1);
        }

        this.inventory.decrementAnimations();
        this.prevCameraYaw = this.cameraYaw;
        super.onLivingUpdate();
        this.landMovementFactor = this.speedOnGround;
        this.jumpMovementFactor = this.speedInAir;

        if (this.isSprinting())
        {
            this.landMovementFactor = (float)((double)this.landMovementFactor + (double)this.speedOnGround * 0.3D);
            this.jumpMovementFactor = (float)((double)this.jumpMovementFactor + (double)this.speedInAir * 0.3D);
        }

        float var1 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
        float var2 = (float)Math.atan(-this.motionY * 0.20000000298023224D) * 15.0F;

        if (var1 > 0.1F)
        {
            var1 = 0.1F;
        }

        if (!this.onGround || this.getHealth() <= 0)
        {
            var1 = 0.0F;
        }

        if (this.onGround || this.getHealth() <= 0)
        {
            var2 = 0.0F;
        }

        this.cameraYaw += (var1 - this.cameraYaw) * 0.4F;
        this.cameraPitch += (var2 - this.cameraPitch) * 0.8F;

        if (this.getHealth() > 0)
        {
            List var3 = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.expand(1.0D, 0.0D, 1.0D));

            if (var3 != null)
            {
                for (int var4 = 0; var4 < var3.size(); ++var4)
                {
                    Entity var5 = (Entity)var3.get(var4);

                    if (!var5.isDead)
                    {
                        this.collideWithPlayer(var5);
                    }
                }
            }
        }
    }

    private void collideWithPlayer(Entity par1Entity)
    {
        par1Entity.onCollideWithPlayer(this);
    }

    public int getScore()
    {
        return this.score;
    }

    /**
     * Called when the mob's health reaches 0.
     */
    public void onDeath(DamageSource par1DamageSource)
    {
        super.onDeath(par1DamageSource);
        this.setSize(0.2F, 0.2F);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.motionY = 0.10000000149011612D;

        if (this.username.equals("Notch"))
        {
            this.dropPlayerItemWithRandomChoice(new ItemStack(Item.appleRed, 1), true);
        }

        this.inventory.dropAllItems();

        if (par1DamageSource != null)
        {
            this.motionX = (double)(-MathHelper.cos((this.attackedAtYaw + this.rotationYaw) * (float)Math.PI / 180.0F) * 0.1F);
            this.motionZ = (double)(-MathHelper.sin((this.attackedAtYaw + this.rotationYaw) * (float)Math.PI / 180.0F) * 0.1F);
        }
        else
        {
            this.motionX = this.motionZ = 0.0D;
        }

        this.yOffset = 0.1F;
        this.addStat(StatList.deathsStat, 1);
    }

    /**
     * Adds a value to the player score. Currently not actually used and the entity passed in does nothing. Args:
     * entity, scoreToAdd
     */
    public void addToPlayerScore(Entity par1Entity, int par2)
    {
        this.score += par2;

        if (par1Entity instanceof EntityPlayer)
        {
            this.addStat(StatList.playerKillsStat, 1);
        }
        else
        {
            this.addStat(StatList.mobKillsStat, 1);
        }
    }

    /**
     * Decrements the entity's air supply when underwater
     */
    protected int decreaseAirSupply(int par1)
    {
        int var2 = EnchantmentHelper.getRespiration(this.inventory);
        return var2 > 0 && this.rand.nextInt(var2 + 1) > 0 ? par1 : super.decreaseAirSupply(par1);
    }

    /**
     * Called when player presses the drop item key
     */
    public EntityItem dropOneItem()
    {
        return this.dropPlayerItemWithRandomChoice(this.inventory.decrStackSize(this.inventory.currentItem, 1), false);
    }

    /**
     * Args: itemstack - called when player drops an item stack that's not in his inventory (like items still placed in
     * a workbench while the workbench'es GUI gets closed)
     */
    public EntityItem dropPlayerItem(ItemStack par1ItemStack)
    {
        return this.dropPlayerItemWithRandomChoice(par1ItemStack, false);
    }

    /**
     * Args: itemstack, flag
     */
    public EntityItem dropPlayerItemWithRandomChoice(ItemStack par1ItemStack, boolean par2)
    {
        if (par1ItemStack == null)
        {
            return null;
        }
        else
        {
            EntityItem var3 = new EntityItem(this.worldObj, this.posX, this.posY - 0.30000001192092896D + (double)this.getEyeHeight(), this.posZ, par1ItemStack);
            var3.delayBeforeCanPickup = 40;
            float var4 = 0.1F;
            float var5;

            if (par2)
            {
                var5 = this.rand.nextFloat() * 0.5F;
                float var6 = this.rand.nextFloat() * (float)Math.PI * 2.0F;
                var3.motionX = (double)(-MathHelper.sin(var6) * var5);
                var3.motionZ = (double)(MathHelper.cos(var6) * var5);
                var3.motionY = 0.20000000298023224D;
            }
            else
            {
                var4 = 0.3F;
                var3.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI) * var4);
                var3.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI) * var4);
                var3.motionY = (double)(-MathHelper.sin(this.rotationPitch / 180.0F * (float)Math.PI) * var4 + 0.1F);
                var4 = 0.02F;
                var5 = this.rand.nextFloat() * (float)Math.PI * 2.0F;
                var4 *= this.rand.nextFloat();
                var3.motionX += Math.cos((double)var5) * (double)var4;
                var3.motionY += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
                var3.motionZ += Math.sin((double)var5) * (double)var4;
            }

            this.joinEntityItemWithWorld(var3);
            this.addStat(StatList.dropStat, 1);
            return var3;
        }
    }

    /**
     * Joins the passed in entity item with the world. Args: entityItem
     */
    protected void joinEntityItemWithWorld(EntityItem par1EntityItem)
    {
        this.worldObj.spawnEntityInWorld(par1EntityItem);
    }

    /**
     * Returns how strong the player is against the specified block at this moment
     */
    public float getCurrentPlayerStrVsBlock(Block par1Block)
    {
        float var2 = this.inventory.getStrVsBlock(par1Block);
        float var3 = var2;
        int var4 = EnchantmentHelper.getEfficiencyModifier(this.inventory);

        if (var4 > 0 && this.inventory.canHarvestBlock(par1Block))
        {
            var3 = var2 + (float)(var4 * var4 + 1);
        }

        if (this.isPotionActive(Potion.digSpeed))
        {
            var3 *= 1.0F + (float)(this.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2F;
        }

        if (this.isPotionActive(Potion.digSlowdown))
        {
            var3 *= 1.0F - (float)(this.getActivePotionEffect(Potion.digSlowdown).getAmplifier() + 1) * 0.2F;
        }

        if (this.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(this.inventory))
        {
            var3 /= 5.0F;
        }

        if (!this.onGround)
        {
            var3 /= 5.0F;
        }

        return var3;
    }

    /**
     * Checks if the player has the ability to harvest a block (checks current inventory item for a tool if necessary)
     */
    public boolean canHarvestBlock(Block par1Block)
    {
        return this.inventory.canHarvestBlock(par1Block);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);
        NBTTagList var2 = par1NBTTagCompound.getTagList("Inventory");
        this.inventory.readFromNBT(var2);
        this.dimension = par1NBTTagCompound.getInteger("Dimension");
        this.sleeping = par1NBTTagCompound.getBoolean("Sleeping");
        this.sleepTimer = par1NBTTagCompound.getShort("SleepTimer");
        this.experience = par1NBTTagCompound.getFloat("XpP");
        this.experienceLevel = par1NBTTagCompound.getInteger("XpLevel");
        this.experienceTotal = par1NBTTagCompound.getInteger("XpTotal");

        if (this.sleeping)
        {
            this.playerLocation = new ChunkCoordinates(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
            this.wakeUpPlayer(true, true, false);
        }

        if (par1NBTTagCompound.hasKey("SpawnX") && par1NBTTagCompound.hasKey("SpawnY") && par1NBTTagCompound.hasKey("SpawnZ"))
        {
            this.spawnChunk = new ChunkCoordinates(par1NBTTagCompound.getInteger("SpawnX"), par1NBTTagCompound.getInteger("SpawnY"), par1NBTTagCompound.getInteger("SpawnZ"));
        }

        this.foodStats.readNBT(par1NBTTagCompound);
        this.capabilities.readCapabilitiesFromNBT(par1NBTTagCompound);
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setTag("Inventory", this.inventory.writeToNBT(new NBTTagList()));
        par1NBTTagCompound.setInteger("Dimension", this.dimension);
        par1NBTTagCompound.setBoolean("Sleeping", this.sleeping);
        par1NBTTagCompound.setShort("SleepTimer", (short)this.sleepTimer);
        par1NBTTagCompound.setFloat("XpP", this.experience);
        par1NBTTagCompound.setInteger("XpLevel", this.experienceLevel);
        par1NBTTagCompound.setInteger("XpTotal", this.experienceTotal);

        if (this.spawnChunk != null)
        {
            par1NBTTagCompound.setInteger("SpawnX", this.spawnChunk.posX);
            par1NBTTagCompound.setInteger("SpawnY", this.spawnChunk.posY);
            par1NBTTagCompound.setInteger("SpawnZ", this.spawnChunk.posZ);
        }

        this.foodStats.writeNBT(par1NBTTagCompound);
        this.capabilities.writeCapabilitiesToNBT(par1NBTTagCompound);
    }

    /**
     * Displays the GUI for interacting with a chest inventory. Args: chestInventory
     */
    public void displayGUIChest(IInventory par1IInventory) {}

    public void displayGUIEnchantment(int par1, int par2, int par3) {}

    /**
     * Displays the crafting GUI for a workbench.
     */
    public void displayWorkbenchGUI(int par1, int par2, int par3) {}

    /**
     * Called whenever an item is picked up from walking over it. Args: pickedUpEntity, stackSize
     */
    public void onItemPickup(Entity par1Entity, int par2) {}

    public float getEyeHeight()
    {
        return 0.12F;
    }

    /**
     * sets the players height back to normal after doing things like sleeping and dieing
     */
    protected void resetHeight()
    {
        this.yOffset = 1.62F;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource par1DamageSource, int par2)
    {
        if (this.capabilities.disableDamage && !par1DamageSource.canHarmInCreative())
        {
            return false;
        }
        else
        {
            this.entityAge = 0;

            if (this.getHealth() <= 0)
            {
                return false;
            }
            else
            {
                if (this.isPlayerSleeping() && !this.worldObj.isRemote)
                {
                    this.wakeUpPlayer(true, true, false);
                }

                Entity var3 = par1DamageSource.getEntity();

                if (var3 instanceof EntityMob || var3 instanceof EntityArrow)
                {
                    if (this.worldObj.difficultySetting == 0)
                    {
                        par2 = 0;
                    }

                    if (this.worldObj.difficultySetting == 1)
                    {
                        par2 = par2 / 2 + 1;
                    }

                    if (this.worldObj.difficultySetting == 3)
                    {
                        par2 = par2 * 3 / 2;
                    }
                }

                if (par2 == 0)
                {
                    return false;
                }
                else
                {
                    Entity var4 = var3;

                    if (var3 instanceof EntityArrow && ((EntityArrow)var3).shootingEntity != null)
                    {
                        var4 = ((EntityArrow)var3).shootingEntity;
                    }

                    if (var4 instanceof EntityLiving)
                    {
                        this.alertWolves((EntityLiving)var4, false);
                    }

                    this.addStat(StatList.damageTakenStat, par2);
                    return super.attackEntityFrom(par1DamageSource, par2);
                }
            }
        }
    }

    /**
     * Reduces damage, depending on potions
     */
    protected int applyPotionDamageCalculations(DamageSource par1DamageSource, int par2)
    {
        int var3 = super.applyPotionDamageCalculations(par1DamageSource, par2);

        if (var3 <= 0)
        {
            return 0;
        }
        else
        {
            int var4 = EnchantmentHelper.getEnchantmentModifierDamage(this.inventory, par1DamageSource);

            if (var4 > 20)
            {
                var4 = 20;
            }

            if (var4 > 0 && var4 <= 20)
            {
                int var5 = 25 - var4;
                int var6 = var3 * var5 + this.carryoverDamage;
                var3 = var6 / 25;
                this.carryoverDamage = var6 % 25;
            }

            return var3;
        }
    }

    /**
     * returns if pvp is enabled or not
     */
    protected boolean isPVPEnabled()
    {
        return false;
    }

    /**
     * Called when the player attack or gets attacked, it's alert all wolves in the area that are owned by the player to
     * join the attack or defend the player.
     */
    protected void alertWolves(EntityLiving par1EntityLiving, boolean par2)
    {
        if (!(par1EntityLiving instanceof EntityCreeper) && !(par1EntityLiving instanceof EntityGhast))
        {
            if (par1EntityLiving instanceof EntityWolf)
            {
                EntityWolf var3 = (EntityWolf)par1EntityLiving;

                if (var3.isTamed() && this.username.equals(var3.getOwnerName()))
                {
                    return;
                }
            }

            if (!(par1EntityLiving instanceof EntityPlayer) || this.isPVPEnabled())
            {
                List var7 = this.worldObj.getEntitiesWithinAABB(EntityWolf.class, AxisAlignedBB.getBoundingBoxFromPool(this.posX, this.posY, this.posZ, this.posX + 1.0D, this.posY + 1.0D, this.posZ + 1.0D).expand(16.0D, 4.0D, 16.0D));
                Iterator var4 = var7.iterator();

                while (var4.hasNext())
                {
                    Entity var5 = (Entity)var4.next();
                    EntityWolf var6 = (EntityWolf)var5;

                    if (var6.isTamed() && var6.getEntityToAttack() == null && this.username.equals(var6.getOwnerName()) && (!par2 || !var6.isSitting()))
                    {
                        var6.setSitting(false);
                        var6.setTarget(par1EntityLiving);
                    }
                }
            }
        }
    }

    protected void damageArmor(int par1)
    {
        this.inventory.damageArmor(par1);
    }

    /**
     * Returns the current armor value as determined by a call to InventoryPlayer.getTotalArmorValue
     */
    public int getTotalArmorValue()
    {
        return this.inventory.getTotalArmorValue();
    }

    /**
     * Deals damage to the entity. If its a EntityPlayer then will take damage from the armor first and then health
     * second with the reduced value. Args: damageAmount
     */
    protected void damageEntity(DamageSource par1DamageSource, int par2)
    {
        if (!par1DamageSource.isUnblockable() && this.isBlocking())
        {
            par2 = 1 + par2 >> 1;
        }

        par2 = this.applyArmorCalculations(par1DamageSource, par2);
        par2 = this.applyPotionDamageCalculations(par1DamageSource, par2);
        this.addExhaustion(par1DamageSource.getHungerDamage());
        this.health -= par2;
    }

    /**
     * Displays the furnace GUI for the passed in furnace entity. Args: tileEntityFurnace
     */
    public void displayGUIFurnace(TileEntityFurnace par1TileEntityFurnace) {}

    /**
     * Displays the dipsenser GUI for the passed in dispenser entity. Args: TileEntityDispenser
     */
    public void displayGUIDispenser(TileEntityDispenser par1TileEntityDispenser) {}

    /**
     * Displays the GUI for editing a sign. Args: tileEntitySign
     */
    public void displayGUIEditSign(TileEntitySign par1TileEntitySign) {}

    /**
     * Displays the GUI for interacting with a brewing stand.
     */
    public void displayGUIBrewingStand(TileEntityBrewingStand par1TileEntityBrewingStand) {}

    /**
     * Uses the currently equipped item on the specified entity. Args: entity
     */
    public void useCurrentItemOnEntity(Entity par1Entity)
    {
        if (!par1Entity.interact(this))
        {
            ItemStack var2 = this.getCurrentEquippedItem();

            if (var2 != null && par1Entity instanceof EntityLiving)
            {
                var2.useItemOnEntity((EntityLiving)par1Entity);

                if (var2.stackSize <= 0)
                {
                    var2.onItemDestroyedByUse(this);
                    this.destroyCurrentEquippedItem();
                }
            }
        }
    }

    /**
     * Returns the currently being used item by the player.
     */
    public ItemStack getCurrentEquippedItem()
    {
        return this.inventory.getCurrentItem();
    }

    /**
     * Destroys the currently equipped item from the player's inventory.
     */
    public void destroyCurrentEquippedItem()
    {
        this.inventory.setInventorySlotContents(this.inventory.currentItem, (ItemStack)null);
    }

    /**
     * Returns the Y Offset of this entity.
     */
    public double getYOffset()
    {
        return (double)(this.yOffset - 0.5F);
    }

    /**
     * Swings the item the player is holding.
     */
    public void swingItem()
    {
        if (!this.isSwinging || this.swingProgressInt >= this.getSwingSpeedModifier() / 2 || this.swingProgressInt < 0)
        {
            this.swingProgressInt = -1;
            this.isSwinging = true;
        }
    }

    /**
     * Attacks for the player the targeted entity with the currently equipped item.  The equipped item has hitEntity
     * called on it. Args: targetEntity
     */
    public void attackTargetEntityWithCurrentItem(Entity par1Entity)
    {
        if (par1Entity.canAttackWithItem())
        {
            int var2 = this.inventory.getDamageVsEntity(par1Entity);

            if (this.isPotionActive(Potion.damageBoost))
            {
                var2 += 3 << this.getActivePotionEffect(Potion.damageBoost).getAmplifier();
            }

            if (this.isPotionActive(Potion.weakness))
            {
                var2 -= 2 << this.getActivePotionEffect(Potion.weakness).getAmplifier();
            }

            int var3 = 0;
            int var4 = 0;

            if (par1Entity instanceof EntityLiving)
            {
                var4 = EnchantmentHelper.getEnchantmentModifierLiving(this.inventory, (EntityLiving)par1Entity);
                var3 += EnchantmentHelper.getKnockbackModifier(this.inventory, (EntityLiving)par1Entity);
            }

            if (this.isSprinting())
            {
                ++var3;
            }

            if (var2 > 0 || var4 > 0)
            {
                boolean var5 = this.fallDistance > 0.0F && !this.onGround && !this.isOnLadder() && !this.isInWater() && !this.isPotionActive(Potion.blindness) && this.ridingEntity == null && par1Entity instanceof EntityLiving;

                if (var5)
                {
                    var2 += this.rand.nextInt(var2 / 2 + 2);
                }

                var2 += var4;
                boolean var6 = par1Entity.attackEntityFrom(DamageSource.causePlayerDamage(this), var2);

                if (var6)
                {
                    if (var3 > 0)
                    {
                        par1Entity.addVelocity((double)(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * (float)var3 * 0.5F), 0.1D, (double)(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * (float)var3 * 0.5F));
                        this.motionX *= 0.6D;
                        this.motionZ *= 0.6D;
                        this.setSprinting(false);
                    }

                    if (var5)
                    {
                        this.onCriticalHit(par1Entity);
                    }

                    if (var4 > 0)
                    {
                        this.onEnchantmentCritical(par1Entity);
                    }

                    if (var2 >= 18)
                    {
                        this.triggerAchievement(AchievementList.overkill);
                    }

                    this.setLastAttackingEntity(par1Entity);
                }

                ItemStack var7 = this.getCurrentEquippedItem();

                if (var7 != null && par1Entity instanceof EntityLiving)
                {
                    var7.hitEntity((EntityLiving)par1Entity, this);

                    if (var7.stackSize <= 0)
                    {
                        var7.onItemDestroyedByUse(this);
                        this.destroyCurrentEquippedItem();
                    }
                }

                if (par1Entity instanceof EntityLiving)
                {
                    if (par1Entity.isEntityAlive())
                    {
                        this.alertWolves((EntityLiving)par1Entity, true);
                    }

                    this.addStat(StatList.damageDealtStat, var2);
                    int var8 = EnchantmentHelper.getFireAspectModifier(this.inventory, (EntityLiving)par1Entity);

                    if (var8 > 0)
                    {
                        par1Entity.setFire(var8 * 4);
                    }
                }

                this.addExhaustion(0.3F);
            }
        }
    }

    /**
     * Called when the player performs a critical hit on the Entity. Args: entity that was hit critically
     */
    public void onCriticalHit(Entity par1Entity) {}

    public void onEnchantmentCritical(Entity par1Entity) {}

    public void respawnPlayer() {}

    public abstract void func_6420_o();

    public void onItemStackChanged(ItemStack par1ItemStack) {}

    /**
     * Will get destroyed next tick.
     */
    public void setDead()
    {
        super.setDead();
        this.inventorySlots.onCraftGuiClosed(this);

        if (this.craftingInventory != null)
        {
            this.craftingInventory.onCraftGuiClosed(this);
        }
    }

    /**
     * Checks if this entity is inside of an opaque block
     */
    public boolean isEntityInsideOpaqueBlock()
    {
        return !this.sleeping && super.isEntityInsideOpaqueBlock();
    }

    /**
     * Attempts to have the player sleep in a bed at the specified location.
     */
    public EnumStatus sleepInBedAt(int par1, int par2, int par3)
    {
        if (!this.worldObj.isRemote)
        {
            if (this.isPlayerSleeping() || !this.isEntityAlive())
            {
                return EnumStatus.OTHER_PROBLEM;
            }

            if (!this.worldObj.worldProvider.func_48217_e())
            {
                return EnumStatus.NOT_POSSIBLE_HERE;
            }

            if (this.worldObj.isDaytime())
            {
                return EnumStatus.NOT_POSSIBLE_NOW;
            }

            if (Math.abs(this.posX - (double)par1) > 3.0D || Math.abs(this.posY - (double)par2) > 2.0D || Math.abs(this.posZ - (double)par3) > 3.0D)
            {
                return EnumStatus.TOO_FAR_AWAY;
            }

            double var4 = 8.0D;
            double var6 = 5.0D;
            List var8 = this.worldObj.getEntitiesWithinAABB(EntityMob.class, AxisAlignedBB.getBoundingBoxFromPool((double)par1 - var4, (double)par2 - var6, (double)par3 - var4, (double)par1 + var4, (double)par2 + var6, (double)par3 + var4));

            if (!var8.isEmpty())
            {
                return EnumStatus.NOT_SAFE;
            }
        }

        this.setSize(0.2F, 0.2F);
        this.yOffset = 0.2F;

        if (this.worldObj.blockExists(par1, par2, par3))
        {
            int var9 = this.worldObj.getBlockMetadata(par1, par2, par3);
            int var5 = BlockBed.getDirection(var9);
            float var10 = 0.5F;
            float var7 = 0.5F;

            switch (var5)
            {
                case 0:
                    var7 = 0.9F;
                    break;

                case 1:
                    var10 = 0.1F;
                    break;

                case 2:
                    var7 = 0.1F;
                    break;

                case 3:
                    var10 = 0.9F;
            }

            this.func_22052_e(var5);
            this.setPosition((double)((float)par1 + var10), (double)((float)par2 + 0.9375F), (double)((float)par3 + var7));
        }
        else
        {
            this.setPosition((double)((float)par1 + 0.5F), (double)((float)par2 + 0.9375F), (double)((float)par3 + 0.5F));
        }

        this.sleeping = true;
        this.sleepTimer = 0;
        this.playerLocation = new ChunkCoordinates(par1, par2, par3);
        this.motionX = this.motionZ = this.motionY = 0.0D;

        if (!this.worldObj.isRemote)
        {
            this.worldObj.updateAllPlayersSleepingFlag();
        }

        return EnumStatus.OK;
    }

    private void func_22052_e(int par1)
    {
        this.field_22063_x = 0.0F;
        this.field_22061_z = 0.0F;

        switch (par1)
        {
            case 0:
                this.field_22061_z = -1.8F;
                break;

            case 1:
                this.field_22063_x = 1.8F;
                break;

            case 2:
                this.field_22061_z = 1.8F;
                break;

            case 3:
                this.field_22063_x = -1.8F;
        }
    }

    /**
     * Wake up the player if they're sleeping.
     */
    public void wakeUpPlayer(boolean par1, boolean par2, boolean par3)
    {
        this.setSize(0.6F, 1.8F);
        this.resetHeight();
        ChunkCoordinates var4 = this.playerLocation;
        ChunkCoordinates var5 = this.playerLocation;

        if (var4 != null && this.worldObj.getBlockId(var4.posX, var4.posY, var4.posZ) == Block.bed.blockID)
        {
            BlockBed.setBedOccupied(this.worldObj, var4.posX, var4.posY, var4.posZ, false);
            var5 = BlockBed.getNearestEmptyChunkCoordinates(this.worldObj, var4.posX, var4.posY, var4.posZ, 0);

            if (var5 == null)
            {
                var5 = new ChunkCoordinates(var4.posX, var4.posY + 1, var4.posZ);
            }

            this.setPosition((double)((float)var5.posX + 0.5F), (double)((float)var5.posY + this.yOffset + 0.1F), (double)((float)var5.posZ + 0.5F));
        }

        this.sleeping = false;

        if (!this.worldObj.isRemote && par2)
        {
            this.worldObj.updateAllPlayersSleepingFlag();
        }

        if (par1)
        {
            this.sleepTimer = 0;
        }
        else
        {
            this.sleepTimer = 100;
        }

        if (par3)
        {
            this.setSpawnChunk(this.playerLocation);
        }
    }

    /**
     * Checks if the player is currently in a bed
     */
    private boolean isInBed()
    {
        return this.worldObj.getBlockId(this.playerLocation.posX, this.playerLocation.posY, this.playerLocation.posZ) == Block.bed.blockID;
    }

    /**
     * Ensure that a block enabling respawning exists at the specified coordinates and find an empty space nearby to
     * spawn.
     */
    public static ChunkCoordinates verifyRespawnCoordinates(World par0World, ChunkCoordinates par1ChunkCoordinates)
    {
        IChunkProvider var2 = par0World.getChunkProvider();
        var2.loadChunk(par1ChunkCoordinates.posX - 3 >> 4, par1ChunkCoordinates.posZ - 3 >> 4);
        var2.loadChunk(par1ChunkCoordinates.posX + 3 >> 4, par1ChunkCoordinates.posZ - 3 >> 4);
        var2.loadChunk(par1ChunkCoordinates.posX - 3 >> 4, par1ChunkCoordinates.posZ + 3 >> 4);
        var2.loadChunk(par1ChunkCoordinates.posX + 3 >> 4, par1ChunkCoordinates.posZ + 3 >> 4);

        if (par0World.getBlockId(par1ChunkCoordinates.posX, par1ChunkCoordinates.posY, par1ChunkCoordinates.posZ) != Block.bed.blockID)
        {
            return null;
        }
        else
        {
            ChunkCoordinates var3 = BlockBed.getNearestEmptyChunkCoordinates(par0World, par1ChunkCoordinates.posX, par1ChunkCoordinates.posY, par1ChunkCoordinates.posZ, 0);
            return var3;
        }
    }

    /**
     * Returns the orientation of the bed in degrees.
     */
    public float getBedOrientationInDegrees()
    {
        if (this.playerLocation != null)
        {
            int var1 = this.worldObj.getBlockMetadata(this.playerLocation.posX, this.playerLocation.posY, this.playerLocation.posZ);
            int var2 = BlockBed.getDirection(var1);

            switch (var2)
            {
                case 0:
                    return 90.0F;

                case 1:
                    return 0.0F;

                case 2:
                    return 270.0F;

                case 3:
                    return 180.0F;
            }
        }

        return 0.0F;
    }

    /**
     * Returns whether player is sleeping or not
     */
    public boolean isPlayerSleeping()
    {
        return this.sleeping;
    }

    /**
     * Returns whether or not the player is asleep and the screen has fully faded.
     */
    public boolean isPlayerFullyAsleep()
    {
        return this.sleeping && this.sleepTimer >= 100;
    }

    public int getSleepTimer()
    {
        return this.sleepTimer;
    }

    /**
     * Add a chat message to the player
     */
    public void addChatMessage(String par1Str) {}

    /**
     * Returns the coordinates to respawn the player based on last bed that the player sleep.
     */
    public ChunkCoordinates getSpawnChunk()
    {
        return this.spawnChunk;
    }

    /**
     * Defines a spawn coordinate to player spawn. Used by bed after the player sleep on it.
     */
    public void setSpawnChunk(ChunkCoordinates par1ChunkCoordinates)
    {
        if (par1ChunkCoordinates != null)
        {
            this.spawnChunk = new ChunkCoordinates(par1ChunkCoordinates);
        }
        else
        {
            this.spawnChunk = null;
        }
    }

    /**
     * Will trigger the specified trigger.
     */
    public void triggerAchievement(StatBase par1StatBase)
    {
        this.addStat(par1StatBase, 1);
    }

    /**
     * Adds a value to a statistic field.
     */
    public void addStat(StatBase par1StatBase, int par2) {}

    /**
     * Causes this entity to do an upwards motion (jumping).
     */
    protected void jump()
    {
        super.jump();
        this.addStat(StatList.jumpStat, 1);

        if (this.isSprinting())
        {
            this.addExhaustion(0.8F);
        }
        else
        {
            this.addExhaustion(0.2F);
        }
    }

    /**
     * Moves the entity based on the specified heading.  Args: strafe, forward
     */
    public void moveEntityWithHeading(float par1, float par2)
    {
        double var3 = this.posX;
        double var5 = this.posY;
        double var7 = this.posZ;

        if (this.capabilities.isFlying)
        {
            double var9 = this.motionY;
            float var11 = this.jumpMovementFactor;
            this.jumpMovementFactor = 0.05F;
            super.moveEntityWithHeading(par1, par2);
            this.motionY = var9 * 0.6D;
            this.jumpMovementFactor = var11;
        }
        else
        {
            super.moveEntityWithHeading(par1, par2);
        }

        this.addMovementStat(this.posX - var3, this.posY - var5, this.posZ - var7);
    }

    /**
     * Adds a value to a movement statistic field - like run, walk, swin or climb.
     */
    public void addMovementStat(double par1, double par3, double par5)
    {
        if (this.ridingEntity == null)
        {
            int var7;

            if (this.isInsideOfMaterial(Material.water))
            {
                var7 = Math.round(MathHelper.sqrt_double(par1 * par1 + par3 * par3 + par5 * par5) * 100.0F);

                if (var7 > 0)
                {
                    this.addStat(StatList.distanceDoveStat, var7);
                    this.addExhaustion(0.015F * (float)var7 * 0.01F);
                }
            }
            else if (this.isInWater())
            {
                var7 = Math.round(MathHelper.sqrt_double(par1 * par1 + par5 * par5) * 100.0F);

                if (var7 > 0)
                {
                    this.addStat(StatList.distanceSwumStat, var7);
                    this.addExhaustion(0.015F * (float)var7 * 0.01F);
                }
            }
            else if (this.isOnLadder())
            {
                if (par3 > 0.0D)
                {
                    this.addStat(StatList.distanceClimbedStat, (int)Math.round(par3 * 100.0D));
                }
            }
            else if (this.onGround)
            {
                var7 = Math.round(MathHelper.sqrt_double(par1 * par1 + par5 * par5) * 100.0F);

                if (var7 > 0)
                {
                    this.addStat(StatList.distanceWalkedStat, var7);

                    if (this.isSprinting())
                    {
                        this.addExhaustion(0.099999994F * (float)var7 * 0.01F);
                    }
                    else
                    {
                        this.addExhaustion(0.01F * (float)var7 * 0.01F);
                    }
                }
            }
            else
            {
                var7 = Math.round(MathHelper.sqrt_double(par1 * par1 + par5 * par5) * 100.0F);

                if (var7 > 25)
                {
                    this.addStat(StatList.distanceFlownStat, var7);
                }
            }
        }
    }

    /**
     * Adds a value to a mounted movement statistic field - by minecart, boat, or pig.
     */
    private void addMountedMovementStat(double par1, double par3, double par5)
    {
        if (this.ridingEntity != null)
        {
            int var7 = Math.round(MathHelper.sqrt_double(par1 * par1 + par3 * par3 + par5 * par5) * 100.0F);

            if (var7 > 0)
            {
                if (this.ridingEntity instanceof EntityMinecart)
                {
                    this.addStat(StatList.distanceByMinecartStat, var7);

                    if (this.startMinecartRidingCoordinate == null)
                    {
                        this.startMinecartRidingCoordinate = new ChunkCoordinates(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
                    }
                    else if (this.startMinecartRidingCoordinate.getEuclideanDistanceTo(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)) >= 1000.0D)
                    {
                        this.addStat(AchievementList.onARail, 1);
                    }
                }
                else if (this.ridingEntity instanceof EntityBoat)
                {
                    this.addStat(StatList.distanceByBoatStat, var7);
                }
                else if (this.ridingEntity instanceof EntityPig)
                {
                    this.addStat(StatList.distanceByPigStat, var7);
                }
            }
        }
    }

    /**
     * Called when the mob is falling. Calculates and applies fall damage.
     */
    protected void fall(float par1)
    {
        if (!this.capabilities.allowFlying)
        {
            if (par1 >= 2.0F)
            {
                this.addStat(StatList.distanceFallenStat, (int)Math.round((double)par1 * 100.0D));
            }

            super.fall(par1);
        }
    }

    /**
     * This method gets called when the entity kills another one.
     */
    public void onKillEntity(EntityLiving par1EntityLiving)
    {
        if (par1EntityLiving instanceof EntityMob)
        {
            this.triggerAchievement(AchievementList.killEnemy);
        }
    }

    /**
     * Gets the Icon Index of the item currently held
     */
    public int getItemIcon(ItemStack par1ItemStack, int par2)
    {
        int var3 = super.getItemIcon(par1ItemStack, par2);

        if (par1ItemStack.itemID == Item.fishingRod.shiftedIndex && this.fishEntity != null)
        {
            var3 = par1ItemStack.getIconIndex() + 16;
        }
        else
        {
            if (par1ItemStack.getItem().requiresMultipleRenderPasses())
            {
                return par1ItemStack.getItem().func_46057_a(par1ItemStack.getItemDamage(), par2);
            }

            if (this.itemInUse != null && par1ItemStack.itemID == Item.bow.shiftedIndex)
            {
                int var4 = par1ItemStack.getMaxItemUseDuration() - this.itemInUseCount;

                if (var4 >= 18)
                {
                    return 133;
                }

                if (var4 > 13)
                {
                    return 117;
                }

                if (var4 > 0)
                {
                    return 101;
                }
            }
        }

        return var3;
    }

    /**
     * Called by portal blocks when an entity is within it.
     */
    public void setInPortal()
    {
        if (this.timeUntilPortal > 0)
        {
            this.timeUntilPortal = 10;
        }
        else
        {
            this.inPortal = true;
        }
    }

    /**
     * This method increases the player's current amount of experience.
     */
    public void addExperience(int par1)
    {
        this.score += par1;
        int var2 = Integer.MAX_VALUE - this.experienceTotal;

        if (par1 > var2)
        {
            par1 = var2;
        }

        this.experience += (float)par1 / (float)this.xpBarCap();

        for (this.experienceTotal += par1; this.experience >= 1.0F; this.experience /= (float)this.xpBarCap())
        {
            this.experience = (this.experience - 1.0F) * (float)this.xpBarCap();
            this.increaseLevel();
        }
    }

    /**
     * Decrease the player level, used to pay levels for enchantments on items at enchanted table.
     */
    public void removeExperience(int par1)
    {
        this.experienceLevel -= par1;

        if (this.experienceLevel < 0)
        {
            this.experienceLevel = 0;
        }
    }

    /**
     * This method returns the cap amount of experience that the experience bar can hold. With each level, the
     * experience cap on the player's experience bar is raised by 10.
     */
    public int xpBarCap()
    {
        return 7 + (this.experienceLevel * 7 >> 1);
    }

    /**
     * This method increases the player's experience level by one.
     */
    private void increaseLevel()
    {
        ++this.experienceLevel;
    }

    /**
     * increases exhaustion level by supplied amount
     */
    public void addExhaustion(float par1)
    {
        if (!this.capabilities.disableDamage)
        {
            if (!this.worldObj.isRemote)
            {
                this.foodStats.addExhaustion(par1);
            }
        }
    }

    /**
     * Returns the player's FoodStats object.
     */
    public FoodStats getFoodStats()
    {
        return this.foodStats;
    }

    public boolean canEat(boolean par1)
    {
        return (par1 || this.foodStats.needFood()) && !this.capabilities.disableDamage;
    }

    /**
     * Checks if the player's health is not full and not zero.
     */
    public boolean shouldHeal()
    {
        return this.getHealth() > 0 && this.getHealth() < this.getMaxHealth();
    }

    /**
     * sets the itemInUse when the use item button is clicked. Args: itemstack, int maxItemUseDuration
     */
    public void setItemInUse(ItemStack par1ItemStack, int par2)
    {
        if (par1ItemStack != this.itemInUse)
        {
            this.itemInUse = par1ItemStack;
            this.itemInUseCount = par2;

            if (!this.worldObj.isRemote)
            {
                this.setEating(true);
            }
        }
    }

    public boolean canPlayerEdit(int par1, int par2, int par3)
    {
        return true;
    }

    /**
     * Get the experience points the entity currently has.
     */
    protected int getExperiencePoints(EntityPlayer par1EntityPlayer)
    {
        int var2 = this.experienceLevel * 7;
        return var2 > 100 ? 100 : var2;
    }

    /**
     * Only use is to identify if class is an instance of player for experience dropping
     */
    protected boolean isPlayer()
    {
        return true;
    }

    public void travelToTheEnd(int par1) {}

    /**
     * Copy the inventory and various stats from another EntityPlayer
     */
    public void copyPlayer(EntityPlayer par1EntityPlayer)
    {
        this.inventory.copyInventory(par1EntityPlayer.inventory);
        this.health = par1EntityPlayer.health;
        this.foodStats = par1EntityPlayer.foodStats;
        this.experienceLevel = par1EntityPlayer.experienceLevel;
        this.experienceTotal = par1EntityPlayer.experienceTotal;
        this.experience = par1EntityPlayer.experience;
        this.score = par1EntityPlayer.score;
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    protected boolean canTriggerWalking()
    {
        return !this.capabilities.isFlying;
    }

    public void func_50009_aI() {}
}
