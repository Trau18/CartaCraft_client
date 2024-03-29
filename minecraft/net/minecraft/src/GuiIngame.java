package net.minecraft.src;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiIngame extends Gui
{
    private static RenderItem itemRenderer = new RenderItem();

    /** A list with all the chat messages in. */
    private List chatMessageList = new ArrayList();

    /** A list with all the sent chat messages in it. */
    private List sentMessageList = new ArrayList();
    private Random rand = new Random();
    private Minecraft mc;
    private int updateCounter = 0;

    /** The string specifying which record music is playing */
    private String recordPlaying = "";

    /** How many ticks the record playing message will be displayed */
    private int recordPlayingUpFor = 0;
    private boolean recordIsPlaying = false;
    private int historyOffset = 0;
    private boolean field_50018_o = false;

    /** Damage partial time (GUI) */
    public float damageGuiPartialTime;

    /** Previous frame vignette brightness (slowly changes by 1% each frame) */
    float prevVignetteBrightness = 1.0F;

    public GuiIngame(Minecraft par1Minecraft)
    {
        this.mc = par1Minecraft;
    }

    /**
     * Render the ingame overlay with quick icon bar, ...
     */
    public void renderGameOverlay(float par1, boolean par2, int par3, int par4)
    {
        ScaledResolution var5 = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
        int var6 = var5.getScaledWidth();
        int var7 = var5.getScaledHeight();
        FontRenderer var8 = this.mc.fontRenderer;
        this.mc.entityRenderer.setupOverlayRendering();
        GL11.glEnable(GL11.GL_BLEND);

        if (Minecraft.isFancyGraphicsEnabled())
        {
            this.renderVignette(this.mc.thePlayer.getBrightness(par1), var6, var7);
        }
        else
        {
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }

        ItemStack var9 = this.mc.thePlayer.inventory.armorItemInSlot(3);

        if (this.mc.gameSettings.thirdPersonView == 0 && var9 != null && var9.itemID == Block.pumpkin.blockID)
        {
            this.renderPumpkinBlur(var6, var7);
        }

        if (!this.mc.thePlayer.isPotionActive(Potion.confusion))
        {
            float var10 = this.mc.thePlayer.prevTimeInPortal + (this.mc.thePlayer.timeInPortal - this.mc.thePlayer.prevTimeInPortal) * par1;

            if (var10 > 0.0F)
            {
                this.renderPortalOverlay(var10, var6, var7);
            }
        }

        boolean var11;
        int var12;
        int var13;
        int var17;
        int var16;
        int var19;
        int var20;
        int var23;
        int var22;
        int var46;

        if (!this.mc.playerController.func_35643_e())
        {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/gui/gui.png"));
            InventoryPlayer var31 = this.mc.thePlayer.inventory;
            this.zLevel = -90.0F;
            this.drawTexturedModalRect(var6 / 2 - 91, var7 - 22, 0, 0, 182, 22);
            this.drawTexturedModalRect(var6 / 2 - 91 - 1 + var31.currentItem * 20, var7 - 22 - 1, 0, 22, 24, 22);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/gui/icons.png"));
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);
            this.drawTexturedModalRect(var6 / 2 - 7, var7 / 2 - 7, 0, 0, 16, 16);
            GL11.glDisable(GL11.GL_BLEND);
            var11 = this.mc.thePlayer.heartsLife / 3 % 2 == 1;

            if (this.mc.thePlayer.heartsLife < 10)
            {
                var11 = false;
            }

            var12 = this.mc.thePlayer.getHealth();
            var13 = this.mc.thePlayer.prevHealth;
            this.rand.setSeed((long)(this.updateCounter * 312871));
            boolean var14 = false;
            FoodStats var15 = this.mc.thePlayer.getFoodStats();
            var16 = var15.getFoodLevel();
            var17 = var15.getPrevFoodLevel();
            this.renderBossHealth();
            int var18;

            if (this.mc.playerController.shouldDrawHUD())
            {
                var18 = var6 / 2 - 91;
                var19 = var6 / 2 + 91;
                var20 = this.mc.thePlayer.xpBarCap();

                if (var20 > 0)
                {
                    short var21 = 182;
                    var22 = (int)(this.mc.thePlayer.experience * (float)(var21 + 1));
                    var23 = var7 - 32 + 3;
                    this.drawTexturedModalRect(var18, var23, 0, 64, var21, 5);

                    if (var22 > 0)
                    {
                        this.drawTexturedModalRect(var18, var23, 0, 69, var22, 5);
                    }
                }

                var46 = var7 - 39;
                var22 = var46 - 10;
                var23 = this.mc.thePlayer.getTotalArmorValue();
                int var24 = -1;

                if (this.mc.thePlayer.isPotionActive(Potion.regeneration))
                {
                    var24 = this.updateCounter % 25;
                }

                int var25;
                int var26;
                int var29;
                int var28;

                for (var25 = 0; var25 < 10; ++var25)
                {
                    if (var23 > 0)
                    {
                        var26 = var18 + var25 * 8;

                        if (var25 * 2 + 1 < var23)
                        {
                            this.drawTexturedModalRect(var26, var22, 34, 9, 9, 9);
                        }

                        if (var25 * 2 + 1 == var23)
                        {
                            this.drawTexturedModalRect(var26, var22, 25, 9, 9, 9);
                        }

                        if (var25 * 2 + 1 > var23)
                        {
                            this.drawTexturedModalRect(var26, var22, 16, 9, 9, 9);
                        }
                    }

                    var26 = 16;

                    if (this.mc.thePlayer.isPotionActive(Potion.poison))
                    {
                        var26 += 36;
                    }

                    byte var27 = 0;

                    if (var11)
                    {
                        var27 = 1;
                    }

                    var28 = var18 + var25 * 8;
                    var29 = var46;

                    if (var12 <= 4)
                    {
                        var29 = var46 + this.rand.nextInt(2);
                    }

                    if (var25 == var24)
                    {
                        var29 -= 2;
                    }

                    byte var30 = 0;

                    if (this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled())
                    {
                        var30 = 5;
                    }

                    this.drawTexturedModalRect(var28, var29, 16 + var27 * 9, 9 * var30, 9, 9);

                    if (var11)
                    {
                        if (var25 * 2 + 1 < var13)
                        {
                            this.drawTexturedModalRect(var28, var29, var26 + 54, 9 * var30, 9, 9);
                        }

                        if (var25 * 2 + 1 == var13)
                        {
                            this.drawTexturedModalRect(var28, var29, var26 + 63, 9 * var30, 9, 9);
                        }
                    }

                    if (var25 * 2 + 1 < var12)
                    {
                        this.drawTexturedModalRect(var28, var29, var26 + 36, 9 * var30, 9, 9);
                    }

                    if (var25 * 2 + 1 == var12)
                    {
                        this.drawTexturedModalRect(var28, var29, var26 + 45, 9 * var30, 9, 9);
                    }
                }

                int var51;

                for (var25 = 0; var25 < 10; ++var25)
                {
                    var26 = var46;
                    var51 = 16;
                    byte var52 = 0;

                    if (this.mc.thePlayer.isPotionActive(Potion.hunger))
                    {
                        var51 += 36;
                        var52 = 13;
                    }

                    if (this.mc.thePlayer.getFoodStats().getSaturationLevel() <= 0.0F && this.updateCounter % (var16 * 3 + 1) == 0)
                    {
                        var26 = var46 + (this.rand.nextInt(3) - 1);
                    }

                    if (var14)
                    {
                        var52 = 1;
                    }

                    var29 = var19 - var25 * 8 - 9;
                    this.drawTexturedModalRect(var29, var26, 16 + var52 * 9, 27, 9, 9);

                    if (var14)
                    {
                        if (var25 * 2 + 1 < var17)
                        {
                            this.drawTexturedModalRect(var29, var26, var51 + 54, 27, 9, 9);
                        }

                        if (var25 * 2 + 1 == var17)
                        {
                            this.drawTexturedModalRect(var29, var26, var51 + 63, 27, 9, 9);
                        }
                    }

                    if (var25 * 2 + 1 < var16)
                    {
                        this.drawTexturedModalRect(var29, var26, var51 + 36, 27, 9, 9);
                    }

                    if (var25 * 2 + 1 == var16)
                    {
                        this.drawTexturedModalRect(var29, var26, var51 + 45, 27, 9, 9);
                    }
                }

                if (this.mc.thePlayer.isInsideOfMaterial(Material.water))
                {
                    var25 = this.mc.thePlayer.getAir();
                    var26 = (int)Math.ceil((double)(var25 - 2) * 10.0D / 300.0D);
                    var51 = (int)Math.ceil((double)var25 * 10.0D / 300.0D) - var26;

                    for (var28 = 0; var28 < var26 + var51; ++var28)
                    {
                        if (var28 < var26)
                        {
                            this.drawTexturedModalRect(var19 - var28 * 8 - 9, var22, 16, 18, 9, 9);
                        }
                        else
                        {
                            this.drawTexturedModalRect(var19 - var28 * 8 - 9, var22, 25, 18, 9, 9);
                        }
                    }
                }
            }

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.enableGUIStandardItemLighting();

            for (var18 = 0; var18 < 9; ++var18)
            {
                var19 = var6 / 2 - 90 + var18 * 20 + 2;
                var20 = var7 - 16 - 3;
                this.renderInventorySlot(var18, var19, var20, par1);
            }

            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        }

        float var33;

        if (this.mc.thePlayer.getSleepTimer() > 0)
        {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            int var32 = this.mc.thePlayer.getSleepTimer();
            var33 = (float)var32 / 100.0F;

            if (var33 > 1.0F)
            {
                var33 = 1.0F - (float)(var32 - 100) / 10.0F;
            }

            var12 = (int)(220.0F * var33) << 24 | 1052704;
            drawRect(0, 0, var6, var7, var12);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }

        int var39;
        int var40;

        if (this.mc.playerController.func_35642_f() && this.mc.thePlayer.experienceLevel > 0)
        {
            var11 = false;
            var12 = var11 ? 16777215 : 8453920;
            String var37 = "" + this.mc.thePlayer.experienceLevel;
            var40 = (var6 - var8.getStringWidth(var37)) / 2;
            var39 = var7 - 31 - 4;
            var8.drawString(var37, var40 + 1, var39, 0);
            var8.drawString(var37, var40 - 1, var39, 0);
            var8.drawString(var37, var40, var39 + 1, 0);
            var8.drawString(var37, var40, var39 - 1, 0);
            var8.drawString(var37, var40, var39, var12);
        }

        if (this.mc.gameSettings.showDebugInfo)
        {
            GL11.glPushMatrix();

            if (Minecraft.hasPaidCheckTime > 0L)
            {
                GL11.glTranslatef(0.0F, 32.0F, 0.0F);
            }

            var8.drawStringWithShadow("Minecraft 1.2.5 (" + this.mc.debug + ")", 2, 2, 16777215);
            var8.drawStringWithShadow(this.mc.debugInfoRenders(), 2, 12, 16777215);
            var8.drawStringWithShadow(this.mc.getEntityDebug(), 2, 22, 16777215);
            var8.drawStringWithShadow(this.mc.debugInfoEntities(), 2, 32, 16777215);
            var8.drawStringWithShadow(this.mc.getWorldProviderName(), 2, 42, 16777215);
            long var38 = Runtime.getRuntime().maxMemory();
            long var34 = Runtime.getRuntime().totalMemory();
            long var41 = Runtime.getRuntime().freeMemory();
            long var42 = var34 - var41;
            String var44 = "Used memory: " + var42 * 100L / var38 + "% (" + var42 / 1024L / 1024L + "MB) of " + var38 / 1024L / 1024L + "MB";
            this.drawString(var8, var44, var6 - var8.getStringWidth(var44) - 2, 2, 14737632);
            var44 = "Allocated memory: " + var34 * 100L / var38 + "% (" + var34 / 1024L / 1024L + "MB)";
            this.drawString(var8, var44, var6 - var8.getStringWidth(var44) - 2, 12, 14737632);
            this.drawString(var8, "x: " + this.mc.thePlayer.posX, 2, 64, 14737632);
            this.drawString(var8, "y: " + this.mc.thePlayer.posY, 2, 72, 14737632);
            this.drawString(var8, "z: " + this.mc.thePlayer.posZ, 2, 80, 14737632);
            this.drawString(var8, "f: " + (MathHelper.floor_double((double)(this.mc.thePlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3), 2, 88, 14737632);
            var46 = MathHelper.floor_double(this.mc.thePlayer.posX);
            var22 = MathHelper.floor_double(this.mc.thePlayer.posY);
            var23 = MathHelper.floor_double(this.mc.thePlayer.posZ);

            if (this.mc.theWorld != null && this.mc.theWorld.blockExists(var46, var22, var23))
            {
                Chunk var47 = this.mc.theWorld.getChunkFromBlockCoords(var46, var23);
                this.drawString(var8, "lc: " + (var47.getTopFilledSegment() + 15) + " b: " + var47.getBiomeGenForWorldCoords(var46 & 15, var23 & 15, this.mc.theWorld.getWorldChunkManager()).biomeName + " bl: " + var47.getSavedLightValue(EnumSkyBlock.Block, var46 & 15, var22, var23 & 15) + " sl: " + var47.getSavedLightValue(EnumSkyBlock.Sky, var46 & 15, var22, var23 & 15) + " rl: " + var47.getBlockLightValue(var46 & 15, var22, var23 & 15, 0), 2, 96, 14737632);
            }

            if (!this.mc.theWorld.isRemote)
            {
                this.drawString(var8, "Seed: " + this.mc.theWorld.getSeed(), 2, 112, 14737632);
            }

            GL11.glPopMatrix();
        }

        if (this.recordPlayingUpFor > 0)
        {
            var33 = (float)this.recordPlayingUpFor - par1;
            var12 = (int)(var33 * 256.0F / 20.0F);

            if (var12 > 255)
            {
                var12 = 255;
            }

            if (var12 > 0)
            {
                GL11.glPushMatrix();
                GL11.glTranslatef((float)(var6 / 2), (float)(var7 - 48), 0.0F);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                var13 = 16777215;

                if (this.recordIsPlaying)
                {
                    var13 = Color.HSBtoRGB(var33 / 50.0F, 0.7F, 0.6F) & 16777215;
                }

                var8.drawString(this.recordPlaying, -var8.getStringWidth(this.recordPlaying) / 2, -4, var13 + (var12 << 24));
                GL11.glDisable(GL11.GL_BLEND);
                GL11.glPopMatrix();
            }
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, (float)(var7 - 48), 0.0F);
        this.renderChatOverlay(var8);
        GL11.glPopMatrix();

        if (this.mc.thePlayer instanceof EntityClientPlayerMP && this.mc.gameSettings.keyBindPlayerList.pressed)
        {
            NetClientHandler var36 = ((EntityClientPlayerMP)this.mc.thePlayer).sendQueue;
            List var35 = var36.playerInfoList;
            var13 = var36.currentServerMaxPlayers;
            var40 = var13;

            for (var39 = 1; var40 > 20; var40 = (var13 + var39 - 1) / var39)
            {
                ++var39;
            }

            var16 = 300 / var39;

            if (var16 > 150)
            {
                var16 = 150;
            }

            var17 = (var6 - var39 * var16) / 2;
            byte var43 = 10;
            drawRect(var17 - 1, var43 - 1, var17 + var16 * var39, var43 + 9 * var40, Integer.MIN_VALUE);

            for (var19 = 0; var19 < var13; ++var19)
            {
                var20 = var17 + var19 % var39 * var16;
                var46 = var43 + var19 / var39 * 9;
                drawRect(var20, var46, var20 + var16 - 1, var46 + 8, 553648127);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glEnable(GL11.GL_ALPHA_TEST);

                if (var19 < var35.size())
                {
                    GuiPlayerInfo var45 = (GuiPlayerInfo)var35.get(var19);
                    var8.drawStringWithShadow(var45.name, var20, var46, 16777215);
                    this.mc.renderEngine.bindTexture(this.mc.renderEngine.getTexture("/gui/icons.png"));
                    byte var50 = 0;
                    boolean var48 = false;
                    byte var49;

                    if (var45.responseTime < 0)
                    {
                        var49 = 5;
                    }
                    else if (var45.responseTime < 150)
                    {
                        var49 = 0;
                    }
                    else if (var45.responseTime < 300)
                    {
                        var49 = 1;
                    }
                    else if (var45.responseTime < 600)
                    {
                        var49 = 2;
                    }
                    else if (var45.responseTime < 1000)
                    {
                        var49 = 3;
                    }
                    else
                    {
                        var49 = 4;
                    }

                    this.zLevel += 100.0F;
                    this.drawTexturedModalRect(var20 + var16 - 12, var46, 0 + var50 * 10, 176 + var49 * 8, 10, 8);
                    this.zLevel -= 100.0F;
                }
            }
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
    }

    private void renderChatOverlay(FontRenderer par1FontRenderer)
    {
        byte var2 = 10;
        boolean var3 = false;
        int var4 = 0;
        int var5 = this.chatMessageList.size();

        if (var5 > 0)
        {
            if (this.isChatOpen())
            {
                var2 = 20;
                var3 = true;
            }

            int var6;
            int var10;

            for (var6 = 0; var6 + this.historyOffset < this.chatMessageList.size() && var6 < var2; ++var6)
            {
                if (((ChatLine)this.chatMessageList.get(var6)).updateCounter < 200 || var3)
                {
                    ChatLine var7 = (ChatLine)this.chatMessageList.get(var6 + this.historyOffset);
                    double var8 = (double)var7.updateCounter / 200.0D;
                    var8 = 1.0D - var8;
                    var8 *= 10.0D;

                    if (var8 < 0.0D)
                    {
                        var8 = 0.0D;
                    }

                    if (var8 > 1.0D)
                    {
                        var8 = 1.0D;
                    }

                    var8 *= var8;
                    var10 = (int)(255.0D * var8);

                    if (var3)
                    {
                        var10 = 255;
                    }

                    ++var4;

                    if (var10 > 2)
                    {
                        byte var11 = 3;
                        int var12 = -var6 * 9;
                        String var13 = var7.message;
                        drawRect(var11, var12 - 1, var11 + 320 + 4, var12 + 8, var10 / 2 << 24);
                        GL11.glEnable(GL11.GL_BLEND);
                        par1FontRenderer.drawStringWithShadow(var13, var11, var12, 16777215 + (var10 << 24));
                    }
                }
            }

            if (var3)
            {
                GL11.glTranslatef(0.0F, (float)par1FontRenderer.FONT_HEIGHT, 0.0F);
                var6 = var5 * par1FontRenderer.FONT_HEIGHT + var5;
                int var14 = var4 * par1FontRenderer.FONT_HEIGHT + var4;
                int var16 = this.historyOffset * var14 / var5;
                int var9 = var14 * var14 / var6;

                if (var6 != var14)
                {
                    var10 = var16 > 0 ? 170 : 96;
                    int var15 = this.field_50018_o ? 13382451 : 3355562;
                    drawRect(0, -var16, 2, -var16 - var9, var15 + (var10 << 24));
                    drawRect(2, -var16, 1, -var16 - var9, 13421772 + (var10 << 24));
                }
            }
        }
    }

    /**
     * Renders dragon's (boss) health on the HUD
     */
    private void renderBossHealth()
    {
        if (RenderDragon.entityDragon != null)
        {
            EntityDragon var1 = RenderDragon.entityDragon;
            RenderDragon.entityDragon = null;
            FontRenderer var2 = this.mc.fontRenderer;
            ScaledResolution var3 = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
            int var4 = var3.getScaledWidth();
            short var5 = 182;
            int var6 = var4 / 2 - var5 / 2;
            int var7 = (int)((float)var1.getDragonHealth() / (float)var1.getMaxHealth() * (float)(var5 + 1));
            byte var8 = 12;
            this.drawTexturedModalRect(var6, var8, 0, 74, var5, 5);
            this.drawTexturedModalRect(var6, var8, 0, 74, var5, 5);

            if (var7 > 0)
            {
                this.drawTexturedModalRect(var6, var8, 0, 79, var7, 5);
            }

            String var9 = "Boss health";
            var2.drawStringWithShadow(var9, var4 / 2 - var2.getStringWidth(var9) / 2, var8 - 10, 16711935);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/gui/icons.png"));
        }
    }

    private void renderPumpkinBlur(int par1, int par2)
    {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("%blur%/misc/pumpkinblur.png"));
        Tessellator var3 = Tessellator.instance;
        var3.startDrawingQuads();
        var3.addVertexWithUV(0.0D, (double)par2, -90.0D, 0.0D, 1.0D);
        var3.addVertexWithUV((double)par1, (double)par2, -90.0D, 1.0D, 1.0D);
        var3.addVertexWithUV((double)par1, 0.0D, -90.0D, 1.0D, 0.0D);
        var3.addVertexWithUV(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);
        var3.draw();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Renders the vignette. Args: vignetteBrightness, width, height
     */
    private void renderVignette(float par1, int par2, int par3)
    {
        par1 = 1.0F - par1;

        if (par1 < 0.0F)
        {
            par1 = 0.0F;
        }

        if (par1 > 1.0F)
        {
            par1 = 1.0F;
        }

        this.prevVignetteBrightness = (float)((double)this.prevVignetteBrightness + (double)(par1 - this.prevVignetteBrightness) * 0.01D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_ONE_MINUS_SRC_COLOR);
        GL11.glColor4f(this.prevVignetteBrightness, this.prevVignetteBrightness, this.prevVignetteBrightness, 1.0F);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("%blur%/misc/vignette.png"));
        Tessellator var4 = Tessellator.instance;
        var4.startDrawingQuads();
        var4.addVertexWithUV(0.0D, (double)par3, -90.0D, 0.0D, 1.0D);
        var4.addVertexWithUV((double)par2, (double)par3, -90.0D, 1.0D, 1.0D);
        var4.addVertexWithUV((double)par2, 0.0D, -90.0D, 1.0D, 0.0D);
        var4.addVertexWithUV(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);
        var4.draw();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Renders the portal overlay. Args: portalStrength, width, height
     */
    private void renderPortalOverlay(float par1, int par2, int par3)
    {
        if (par1 < 1.0F)
        {
            par1 *= par1;
            par1 *= par1;
            par1 = par1 * 0.8F + 0.2F;
        }

        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, par1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mc.renderEngine.getTexture("/terrain.png"));
        float var4 = (float)(Block.portal.blockIndexInTexture % 16) / 16.0F;
        float var5 = (float)(Block.portal.blockIndexInTexture / 16) / 16.0F;
        float var6 = (float)(Block.portal.blockIndexInTexture % 16 + 1) / 16.0F;
        float var7 = (float)(Block.portal.blockIndexInTexture / 16 + 1) / 16.0F;
        Tessellator var8 = Tessellator.instance;
        var8.startDrawingQuads();
        var8.addVertexWithUV(0.0D, (double)par3, -90.0D, (double)var4, (double)var7);
        var8.addVertexWithUV((double)par2, (double)par3, -90.0D, (double)var6, (double)var7);
        var8.addVertexWithUV((double)par2, 0.0D, -90.0D, (double)var6, (double)var5);
        var8.addVertexWithUV(0.0D, 0.0D, -90.0D, (double)var4, (double)var5);
        var8.draw();
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Renders the specified item of the inventory slot at the specified location. Args: slot, x, y, partialTick
     */
    private void renderInventorySlot(int par1, int par2, int par3, float par4)
    {
        ItemStack var5 = this.mc.thePlayer.inventory.mainInventory[par1];

        if (var5 != null)
        {
            float var6 = (float)var5.animationsToGo - par4;

            if (var6 > 0.0F)
            {
                GL11.glPushMatrix();
                float var7 = 1.0F + var6 / 5.0F;
                GL11.glTranslatef((float)(par2 + 8), (float)(par3 + 12), 0.0F);
                GL11.glScalef(1.0F / var7, (var7 + 1.0F) / 2.0F, 1.0F);
                GL11.glTranslatef((float)(-(par2 + 8)), (float)(-(par3 + 12)), 0.0F);
            }

            itemRenderer.renderItemIntoGUI(this.mc.fontRenderer, this.mc.renderEngine, var5, par2, par3);

            if (var6 > 0.0F)
            {
                GL11.glPopMatrix();
            }

            itemRenderer.renderItemOverlayIntoGUI(this.mc.fontRenderer, this.mc.renderEngine, var5, par2, par3);
        }
    }

    /**
     * The update tick for the ingame UI
     */
    public void updateTick()
    {
        if (this.recordPlayingUpFor > 0)
        {
            --this.recordPlayingUpFor;
        }

        ++this.updateCounter;

        for (int var1 = 0; var1 < this.chatMessageList.size(); ++var1)
        {
            ++((ChatLine)this.chatMessageList.get(var1)).updateCounter;
        }
    }

    /**
     * Clear all chat messages.
     */
    public void clearChatMessages()
    {
        this.chatMessageList.clear();
        this.sentMessageList.clear();
    }

    /**
     * Adds a chat message to the list of chat messages. Args: msg
     */
    public void addChatMessage(String par1Str)
    {
        boolean var2 = this.isChatOpen();
        boolean var3 = true;
        Iterator var4 = this.mc.fontRenderer.listFormattedStringToWidth(par1Str, 320).iterator();

        while (var4.hasNext())
        {
            String var5 = (String)var4.next();

            if (var2 && this.historyOffset > 0)
            {
                this.field_50018_o = true;
                this.adjustHistoryOffset(1);
            }

            if (!var3)
            {
                var5 = " " + var5;
            }

            var3 = false;
            this.chatMessageList.add(0, new ChatLine(var5));
        }

        while (this.chatMessageList.size() > 100)
        {
            this.chatMessageList.remove(this.chatMessageList.size() - 1);
        }
    }

    /**
     * Returns the list with the sent chat messages in it.
     */
    public List getSentMessageList()
    {
        return this.sentMessageList;
    }

    public void func_50014_d()
    {
        this.historyOffset = 0;
        this.field_50018_o = false;
    }

    /**
     * increment/decrement history scroll offset
     */
    public void adjustHistoryOffset(int par1)
    {
        this.historyOffset += par1;
        int var2 = this.chatMessageList.size();

        if (this.historyOffset > var2 - 20)
        {
            this.historyOffset = var2 - 20;
        }

        if (this.historyOffset <= 0)
        {
            this.historyOffset = 0;
            this.field_50018_o = false;
        }
    }

    /**
     * gets the click data from mouse position args:( mouse x, mouse y)
     */
    public ChatClickData getChatClickDataFromMouse(int par1, int par2)
    {
        if (!this.isChatOpen())
        {
            return null;
        }
        else
        {
            ScaledResolution var3 = new ScaledResolution(this.mc.gameSettings, this.mc.displayWidth, this.mc.displayHeight);
            par2 = par2 / var3.scaleFactor - 40;
            par1 = par1 / var3.scaleFactor - 3;

            if (par1 >= 0 && par2 >= 0)
            {
                int var4 = Math.min(20, this.chatMessageList.size());

                if (par1 <= 320 && par2 < this.mc.fontRenderer.FONT_HEIGHT * var4 + var4)
                {
                    int var5 = par2 / (this.mc.fontRenderer.FONT_HEIGHT + 1) + this.historyOffset;
                    return new ChatClickData(this.mc.fontRenderer, (ChatLine)this.chatMessageList.get(var5), par1, par2 - (var5 - this.historyOffset) * this.mc.fontRenderer.FONT_HEIGHT + var5);
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
    }

    public void setRecordPlayingMessage(String par1Str)
    {
        this.recordPlaying = "Now playing: " + par1Str;
        this.recordPlayingUpFor = 60;
        this.recordIsPlaying = true;
    }

    /**
     * Return true if chat gui is open
     */
    public boolean isChatOpen()
    {
        return this.mc.currentScreen instanceof GuiChat;
    }

    /**
     * Adds the string to chat message after translate it with the language file.
     */
    public void addChatMessageTranslate(String par1Str)
    {
        StringTranslate var2 = StringTranslate.getInstance();
        String var3 = var2.translateKey(par1Str);
        this.addChatMessage(var3);
    }
}
