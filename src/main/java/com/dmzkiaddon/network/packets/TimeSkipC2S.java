package com.dmzkiaddon.network.packets;

import com.dmzkiaddon.registry.ModSounds;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C2S — Salto en el Tiempo de Hit.
 *
 * Al activarse:
 *  - Todas las entidades vivas en un radio de 16 bloques quedan paralizadas
 *    durante 5 segundos (100 ticks, Slowness 255 + Jump Boost negativo).
 *  - El jugador NO se mueve automáticamente — se desplaza libremente
 *    y golpea a su elección durante esa ventana.
 *  - El daño de los golpes durante la ventana escala con SKP del jugador
 *    (gestionado en el cliente via overlay; el servidor solo aplica el freeze).
 *
 * No consume Ki.
 */
public class TimeSkipC2S {

    /** Duración del congelamiento en ticks (5 segundos). */
    private static final int FREEZE_TICKS = 100;

    /** Radio de efecto en bloques. */
    private static final double FREEZE_RADIUS = 16.0;

    public TimeSkipC2S() {}

    public TimeSkipC2S(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public static void handle(TimeSkipC2S packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            // Congelar todas las entidades vivas cercanas excepto el propio jugador
            player.level()
                    .getEntitiesOfClass(LivingEntity.class,
                            player.getBoundingBox().inflate(FREEZE_RADIUS),
                            e -> !e.equals(player) && e.isAlive())
                    .forEach(e -> {
                        // Slowness 255 → velocidad de movimiento = 0
                        e.addEffect(new MobEffectInstance(
                                MobEffects.MOVEMENT_SLOWDOWN, FREEZE_TICKS, 255, false, false));
                        // Jump Boost -10 → impide saltar durante el freeze
                        e.addEffect(new MobEffectInstance(
                                MobEffects.JUMP, FREEZE_TICKS, -10, false, false));
                    });

            // Sonido seco de activación (estilo golpe preciso de Hit)
            player.playSound(ModSounds.BLAST.get(), 0.7f, 1.8f);
            player.level().playSound(player,
                    player.getX(), player.getY(), player.getZ(),
                    ModSounds.BLAST.get(),
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    0.7f, 1.8f);
        });
        ctx.setPacketHandled(true);
    }
}