/*
 * Work under Copyright. Licensed under the EUPL.
 * See the project README.md and LICENSE.txt for more information.
 */

package net.dries007.tfc.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import io.netty.buffer.ByteBuf;
import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.api.recipes.AnvilRecipe;
import net.dries007.tfc.api.registries.TFCRegistries;
import net.dries007.tfc.objects.te.TEAnvilTFC;
import net.dries007.tfc.util.Helpers;

public class PacketAnvilRecipe implements IMessage
{
    private BlockPos pos;
    private ResourceLocation recipe;

    // no args constructor required for forge
    @SuppressWarnings("unused")
    public PacketAnvilRecipe() {}

    public PacketAnvilRecipe(TEAnvilTFC tile)
    {
        this.pos = tile.getPos();
        AnvilRecipe recipe = tile.getRecipe();
        this.recipe = recipe != null ? recipe.getRegistryName() : null;
    }

    @Override
    public void fromBytes(ByteBuf buffer)
    {
        pos = BlockPos.fromLong(buffer.readLong());
        if (buffer.readBoolean())
        {
            recipe = new ResourceLocation(ByteBufUtils.readUTF8String(buffer));
        }
        else
        {
            recipe = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buffer)
    {
        buffer.writeLong(pos.toLong());
        buffer.writeBoolean(recipe != null);
        if (recipe != null)
        {
            ByteBufUtils.writeUTF8String(buffer, recipe.toString());
        }
    }

    public static class Handler implements IMessageHandler<PacketAnvilRecipe, IMessage>
    {
        @Override
        public IMessage onMessage(PacketAnvilRecipe message, MessageContext ctx)
        {
            EntityPlayer player = TerraFirmaCraft.getProxy().getPlayer(ctx);
            World world = player.getEntityWorld();
            TerraFirmaCraft.getProxy().getThreadListener(ctx).addScheduledTask(() -> {
                TEAnvilTFC te = Helpers.getTE(world, message.pos, TEAnvilTFC.class);
                if (te != null)
                {
                    if (message.recipe == null)
                    {
                        te.setRecipe(null, false);
                    }
                    else
                    {
                        te.setRecipe(TFCRegistries.ANVIL.getValue(message.recipe), false);
                    }
                }
            });
            return null;
        }
    }
}
