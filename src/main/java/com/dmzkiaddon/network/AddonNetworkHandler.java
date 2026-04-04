package com.dmzkiaddon.network;

import com.dmzkiaddon.network.packets.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.dmzkiaddon.DMZKiAddon.MOD_ID;

/**
 * AddonNetworkHandler — registro centralizado y seguro de paquetes.
 *
 * Para agregar un paquete nuevo:
 *   1. Crea la clase del paquete con encode(), decode (constructor buf) y handle().
 *   2. Agrega UNA línea aquí con reg(...).
 *   ¡Eso es todo! No puedes olvidar registrarlo si lo haces en este método.
 */
public class AddonNetworkHandler {

    private static final Logger LOGGER = LogManager.getLogger("DMZKiAddon/Network");
    private static final String PROTOCOL = "1";

    public static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int registeredCount = 0;

    // -------------------------------------------------------------------------
    //  REGISTRO DE PAQUETES — agrega aquí cada paquete nuevo
    // -------------------------------------------------------------------------
    public static void register() {
        INSTANCE = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(MOD_ID, "network"))
                .networkProtocolVersion(() -> PROTOCOL)
                .clientAcceptedVersions(PROTOCOL::equals)
                .serverAcceptedVersions(PROTOCOL::equals)
                .simpleChannel();

        // C2S — cliente → servidor
        reg(FireKiAttackC2S.class,          FireKiAttackC2S::new,          FireKiAttackC2S::encode,          FireKiAttackC2S::handle,          NetworkDirection.PLAY_TO_SERVER);
        reg(ToggleKiShieldC2S.class,        ToggleKiShieldC2S::new,        ToggleKiShieldC2S::encode,        ToggleKiShieldC2S::handle,        NetworkDirection.PLAY_TO_SERVER);
        reg(SpawnHellzoneC2S.class,         SpawnHellzoneC2S::new,         SpawnHellzoneC2S::encode,         SpawnHellzoneC2S::handle,         NetworkDirection.PLAY_TO_SERVER);
        reg(LaunchHellzoneC2S.class,        LaunchHellzoneC2S::new,        LaunchHellzoneC2S::encode,        LaunchHellzoneC2S::handle,        NetworkDirection.PLAY_TO_SERVER);
        reg(InitiateHakaiC2S.class,         InitiateHakaiC2S::new,         InitiateHakaiC2S::encode,         InitiateHakaiC2S::handle,         NetworkDirection.PLAY_TO_SERVER);
        reg(HakaiKeyPressC2S.class,         HakaiKeyPressC2S::new,         HakaiKeyPressC2S::encode,         HakaiKeyPressC2S::handle,         NetworkDirection.PLAY_TO_SERVER);
        reg(TimeSkipC2S.class,              TimeSkipC2S::new,              TimeSkipC2S::encode,              TimeSkipC2S::handle,              NetworkDirection.PLAY_TO_SERVER);
        reg(PointPressureC2S.class,         PointPressureC2S::new,         PointPressureC2S::encode,         PointPressureC2S::handle,         NetworkDirection.PLAY_TO_SERVER);
        reg(FinalExplosionChargeC2S.class,  FinalExplosionChargeC2S::new,  FinalExplosionChargeC2S::encode,  FinalExplosionChargeC2S::handle,  NetworkDirection.PLAY_TO_SERVER);
        reg(FinalExplosionC2S.class,        FinalExplosionC2S::new,        FinalExplosionC2S::encode,        FinalExplosionC2S::handle,        NetworkDirection.PLAY_TO_SERVER);
        reg(FinalExplosionCancelC2S.class,  FinalExplosionCancelC2S::new,  FinalExplosionCancelC2S::encode,  FinalExplosionCancelC2S::handle,  NetworkDirection.PLAY_TO_SERVER);
        reg(KikohoC2S.class,                KikohoC2S::new,                KikohoC2S::encode,                KikohoC2S::handle,                NetworkDirection.PLAY_TO_SERVER);
        reg(NeoKikohoC2S.class,             NeoKikohoC2S::new,             NeoKikohoC2S::encode,             NeoKikohoC2S::handle,             NetworkDirection.PLAY_TO_SERVER);
        reg(NeoKikohoResetC2S.class,        NeoKikohoResetC2S::new,        NeoKikohoResetC2S::encode,        NeoKikohoResetC2S::handle,        NetworkDirection.PLAY_TO_SERVER);
        reg(CreateCustomAttackC2S.class,    CreateCustomAttackC2S::new,    CreateCustomAttackC2S::encode,    CreateCustomAttackC2S::handle,    NetworkDirection.PLAY_TO_SERVER);

        // S2C — servidor → cliente
        reg(HakaiUpdateS2C.class,           HakaiUpdateS2C::new,           HakaiUpdateS2C::encode,           HakaiUpdateS2C::handle,           NetworkDirection.PLAY_TO_CLIENT);
        reg(ImpactShakeS2C.class,           ImpactShakeS2C::new,           ImpactShakeS2C::encode,           ImpactShakeS2C::handle,           NetworkDirection.PLAY_TO_CLIENT);
        reg(SyncAttackRegistryS2C.class,    SyncAttackRegistryS2C::new,    SyncAttackRegistryS2C::encode,    SyncAttackRegistryS2C::handle,    NetworkDirection.PLAY_TO_CLIENT);
        reg(SyncSkillLevelS2C.class,        SyncSkillLevelS2C::new,        SyncSkillLevelS2C::encode,        SyncSkillLevelS2C::handle,        NetworkDirection.PLAY_TO_CLIENT);

        LOGGER.info("[DMZKiAddon] {} paquetes registrados correctamente.", registeredCount);
    }

    // -------------------------------------------------------------------------
    //  HELPERS — no tocar
    // -------------------------------------------------------------------------

    /**
     * Registra un paquete de forma segura. Si falla, lanza un error descriptivo
     * en lugar del IllegalArgumentException genérico de Forge.
     */
    private static <MSG> void reg(
            Class<MSG> clazz,
            Function<FriendlyByteBuf, MSG> decoder,
            BiConsumer<MSG, FriendlyByteBuf> encoder,
            BiConsumer<MSG, Supplier<NetworkEvent.Context>> handler,
            NetworkDirection direction
    ) {
        int id = packetId++;
        try {
            INSTANCE.messageBuilder(clazz, id, direction)
                    .decoder(decoder)
                    .encoder(encoder)
                    .consumerMainThread(handler)
                    .add();
            registeredCount++;
            LOGGER.debug("[DMZKiAddon] Paquete registrado: {} (id={})", clazz.getSimpleName(), id);
        } catch (Exception e) {
            // Error claro: sabes exactamente qué paquete falló y por qué
            throw new IllegalStateException(
                "[DMZKiAddon] ERROR al registrar el paquete '" + clazz.getSimpleName() +
                "' con id=" + id + ". Causa: " + e.getMessage(), e
            );
        }
    }

    // -------------------------------------------------------------------------
    //  ENVÍO SEGURO — usa estos en lugar de INSTANCE.send(...) directamente
    // -------------------------------------------------------------------------

    /** Envía un paquete al servidor con manejo de error claro. */
    public static <MSG> void sendToServer(MSG message) {
        if (INSTANCE == null) {
            LOGGER.error("[DMZKiAddon] Intento de enviar {} al servidor antes de registrar el canal.",
                    message.getClass().getSimpleName());
            return;
        }
        try {
            INSTANCE.sendToServer(message);
        } catch (Exception e) {
            LOGGER.error("[DMZKiAddon] Fallo al enviar {} al servidor. ¿Está registrado el paquete?",
                    message.getClass().getSimpleName(), e);
        }
    }

    /** Envía un paquete a un jugador específico con manejo de error claro. */
    public static <MSG> void sendToPlayer(MSG message, net.minecraft.server.level.ServerPlayer player) {
        if (INSTANCE == null) {
            LOGGER.error("[DMZKiAddon] Intento de enviar {} a {} antes de registrar el canal.",
                    message.getClass().getSimpleName(), player.getName().getString());
            return;
        }
        try {
            INSTANCE.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), message);
        } catch (Exception e) {
            LOGGER.error("[DMZKiAddon] Fallo al enviar {} al jugador {}. ¿Está registrado el paquete?",
                    message.getClass().getSimpleName(), player.getName().getString(), e);
        }
    }

    /** Envía un paquete a todos los jugadores (broadcast). */
    public static <MSG> void sendToAll(MSG message) {
        if (INSTANCE == null) {
            LOGGER.error("[DMZKiAddon] Intento de broadcast de {} antes de registrar el canal.",
                    message.getClass().getSimpleName());
            return;
        }
        try {
            INSTANCE.send(net.minecraftforge.network.PacketDistributor.ALL.noArg(), message);
        } catch (Exception e) {
            LOGGER.error("[DMZKiAddon] Fallo en broadcast de {}. ¿Está registrado el paquete?",
                    message.getClass().getSimpleName(), e);
        }
    }
}