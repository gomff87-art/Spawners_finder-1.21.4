package com.example.spawnerfinder;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashSet;
import java.util.Set;

public class SpawnerFinderClient implements ClientModInitializer {

    private static final int RANGE_SIDE = 100;
    private static final int RANGE_DOWN = 100;
    private static final int RANGE_UP = 20;
    private static final int WEB_RADIUS = 4;

    private static boolean enabled = false;
    private static int tick = 0;
    private static final Set<Long> reported = new HashSet<>();

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("spawners")
                        .then(ClientCommandManager.literal("start").executes(ctx -> {
                            enabled = true;
                            reported.clear();
                            ctx.getSource().sendFeedback(Text.literal("Поиск спавнеров включён"));
                            return Command.SINGLE_SUCCESS;
                        }))
                        .then(ClientCommandManager.literal("stop").executes(ctx -> {
                            enabled = false;
                            ctx.getSource().sendFeedback(Text.literal("Поиск спавнеров выключен"));
                            return Command.SINGLE_SUCCESS;
                        }))));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!enabled || client.world == null || client.player == null) return;
            if (++tick % 20 != 0) return;
            scan(client);
        });
    }

    private static void scan(MinecraftClient client) {
        ClientWorld world = client.world;
        ClientPlayerEntity player = client.player;
        BlockPos p = player.getBlockPos();

        int minCx = (p.getX() - RANGE_SIDE) >> 4;
        int maxCx = (p.getX() + RANGE_SIDE) >> 4;
        int minCz = (p.getZ() - RANGE_SIDE) >> 4;
        int maxCz = (p.getZ() + RANGE_SIDE) >> 4;

        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                WorldChunk chunk = world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;

                for (BlockPos pos : chunk.getBlockEntityPositions()) {
                    if (Math.abs(pos.getX() - p.getX()) > RANGE_SIDE) continue;
                    if (Math.abs(pos.getZ() - p.getZ()) > RANGE_SIDE) continue;
