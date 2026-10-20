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

/**
 * /spawners start - включает поиск спавнеров с паутиной рядом.
 * /spawners stop  - выключает поиск.
 * Код написан под Yarn-маппинги.
 */
public class SpawnerFinderClient implements ClientModInitializer {

    // Радиус поиска в блоках
    private static final int RANGE_SIDE = 100; // в стороны
    private static final int RANGE_DOWN = 100; // вниз
    private static final int RANGE_UP = 20;    // вверх
    private static final int WEB_RADIUS = 4;   // где искать паутину рядом со спавнером

    private static boolean enabled = false;
    private static int tick = 0;
    private static final Set<Long> reported = new HashSet<>();

    @Override
    public void onInitializeClient() {
