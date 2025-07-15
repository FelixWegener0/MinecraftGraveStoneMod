package org.felixWegener.gravestone;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class Gravestone implements ModInitializer {

    private boolean delayed = false;
    private int count = 0;
    private List<ItemStack> inventoryItems;
    private BlockPos pos;

    @Override
    public void onInitialize() {
        System.out.println("Initializing Gravestone");
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            if (entity instanceof PlayerEntity) {
                if(source.getAttacker() instanceof CreeperEntity) {
                    PlayerEntity player = (PlayerEntity) entity;

                    this.delayed = true;
                    this.inventoryItems = new ArrayList<>();
                    for (int i = 0; i < player.getInventory().size(); i++) {
                        ItemStack stack = player.getInventory().getStack(i);
                        if (!stack.isEmpty()) {
                            this.inventoryItems.add(stack.copy());
                        }
                    }
                    this.pos = player.getBlockPos();
                    player.getInventory().clear();
                    player.sendMessage(Text.literal("Death position: " + pos.getX() + ' ' + pos.getY() + ' ' + pos.getZ()).formatted(Formatting.AQUA), false);
                } else {
                    createGravestone((PlayerEntity) entity);
                }
            }
            return true;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (delayed) {
                this.count++;
            }
            if (this.count > 5) {
                createGravestoneByBlock(this.pos, this.inventoryItems, server.getWorld(World.OVERWORLD));
                this.delayed = false;
                this.count = 0;
            }
        });
    }

    private void createGravestone(PlayerEntity player) {
        BlockPos pos = player.getBlockPos();
        BlockPos pos2 = pos.up();

        World world = player.getEntityWorld();

        world.setBlockState(pos, Blocks.CHEST.getDefaultState());
        world.setBlockState(pos2, Blocks.CHEST.getDefaultState());

        BlockEntity blockEntity = world.getBlockEntity(pos);
        BlockEntity blockEntity2 = world.getBlockEntity(pos2);

        if (blockEntity instanceof ChestBlockEntity chest && blockEntity2 instanceof ChestBlockEntity chest2) {

            Inventory inventory = player.getInventory();
            List<ItemStack> items = new ArrayList<ItemStack>();
            int count = 0;

            for  (int i = 0; i < inventory.size(); i++) {
                ItemStack itemStack = inventory.getStack(i);
                if (!itemStack.isEmpty()) {
                    items.add(itemStack);
                }
            }

            chestSet(items, world, pos2, chest, chest2, count);

            player.getInventory().clear();
            player.sendMessage(Text.literal("Death position: " + pos.getX() + ' ' + pos.getY() + ' ' + pos.getZ()).formatted(Formatting.AQUA), false);
        }
    }

    private void createGravestoneByBlock(BlockPos pos, List<ItemStack> inventoryItems, World world) {
        BlockPos pos2 = pos.up();

        world.setBlockState(pos, Blocks.CHEST.getDefaultState());
        world.setBlockState(pos2, Blocks.CHEST.getDefaultState());

        BlockEntity blockEntity = world.getBlockEntity(pos);
        BlockEntity blockEntity2 = world.getBlockEntity(pos2);

        if (blockEntity instanceof ChestBlockEntity chest && blockEntity2 instanceof ChestBlockEntity chest2) {
            int count = 0;

            chestSet(inventoryItems, world, pos2, chest, chest2, count);
        }
    }

    private void chestSet(List<ItemStack> inventoryItems, World world, BlockPos pos2, ChestBlockEntity chest, ChestBlockEntity chest2, int count) {
        if (inventoryItems.size() < 27) {
            world.setBlockState(pos2, Blocks.AIR.getDefaultState());
            for (int i = 0; i < inventoryItems.size(); i++) {
                chest.setStack(i, inventoryItems.get(i));
            }
        } else {
            for (int i = 0; i < 27; i++) {
                chest.setStack(i, inventoryItems.get(i));
            }
            for (int i = 27; i < inventoryItems.size(); i++) {
                chest2.setStack(count, inventoryItems.get(i));
                count++;
            }
        }
    }
}
