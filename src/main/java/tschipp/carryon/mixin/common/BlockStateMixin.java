package tschipp.carryon.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import tschipp.carryon.PickupHandler;
import tschipp.carryon.RegistryHandler;
import tschipp.carryon.items.ItemTile;

@Mixin(BlockState.class)
public class BlockStateMixin {


    //Detects right click detection
    @Inject(method = "activate", at = @At("HEAD"), cancellable = true)
    public void onBlockActivated(World world, BlockPos pos, PlayerEntity player, Hand hand, Direction Direction, float hitx, float hity, float hitz, CallbackInfoReturnable<Boolean> info) {
        ItemStack main = player.getMainHandStack();
        ItemStack off = player.getOffHandStack();

        if(main.isEmpty() && off.isEmpty() && player.isSneaking())
        {
            info.cancel();
            info.setReturnValue(false);

			BlockState state = world.getBlockState(pos);
            ItemStack stack = new ItemStack(RegistryHandler.TILE_ITEM);

            BlockEntity te = world.getBlockEntity(pos);
				if (PickupHandler.canPlayerPickUpBlock(player, te, world, pos))
				{
					// player.open.closeScreen();

					if (ItemTile.storeTileData(te, world, pos, state, stack))
					{

						// BlockState statee = world.getBlockState(pos);
						CompoundTag tag = new CompoundTag();
						tag = world.getBlockEntity(pos) != null ? world.getBlockEntity(pos).toTag(tag) : new CompoundTag();
						// CarryOnOverride override = ScriptChecker.inspectBlock(state, world, pos, tag);
						// int overrideHash = 0;
						// if (override != null)
						// 	overrideHash = override.hashCode();

						// positions.put(pos, 0);

						// boolean success = false;

						try
						{
							// sendPacket(player, player.inventory.currentItem, overrideHash);

							world.removeBlockEntity(pos);
							world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
							player.setStackInHand(Hand.MAIN, stack);
							// event.setUseBlock(Result.DENY);
							// event.setUseItem(Result.DENY);
							// event.setCanceled(true);
							// success = true;
						}
						catch (Exception e)
						{
							// try
							// {
							// 	sendPacket(player, player.inventory.currentItem, overrideHash);
							// 	emptyBlockEntity(te);
							// 	world.setBlockToAir(pos);
							// 	player.setHeldItem(EnumHand.MAIN_HAND, stack);
							// 	event.setUseBlock(Result.DENY);
							// 	event.setUseItem(Result.DENY);
							// 	event.setCanceled(true);
							// 	success = true;
							// }
							// catch (Exception ex)
							// {
							// 	sendPacket(player, 9, 0);
							// 	world.setBlockState(pos, statee);
							// 	if (!tag.hasNoTags())
							// 		BlockEntity.create(world, tag);

							// 	player.sendMessage(new TextComponentString(TextFormatting.RED + "Error detected. Cannot pick up block."));
							// 	TextComponentString s = new TextComponentString(TextFormatting.GOLD + "here");
							// 	s.getStyle().setClickEvent(new ClickEvent(Action.OPEN_URL, "https://github.com/Tschipp/CarryOn/issues"));
							// 	player.sendMessage(new TextComponentString(TextFormatting.RED + "Please report this error ").appendSibling(s));
							// }

						}

						// if (success && override != null)
						// {
						// 	String command = override.getCommandInit();
						// 	if (command != null)
						// 		player.getServer().getCommandManager().executeCommand(player.getServer(), "/execute " + player.getGameProfile().getName() + " ~ ~ ~ " + command);
						// }

					}
				}
		}
		else if(!main.isEmpty() && main.getItem() == RegistryHandler.TILE_ITEM || main.getItem() == RegistryHandler.ENTITY_ITEM)
		{
			info.cancel();
		}
     }


}
