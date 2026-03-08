package com.dmzkiaddon.entity;

import com.dragonminez.common.init.MainEntities;
import com.dragonminez.common.init.entities.ki.AbstractKiProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import com.dmzkiaddon.ModSounds;
import net.minecraft.sounds.SoundSource;

public class KiBlastAddon extends AbstractKiProjectile {

    public enum SoundType { SPIRIT_BOMB, BIG_BANG, DEATH_BALL, GENERIC }

    private boolean hasSpawnedSplash = false;
    private boolean hasSpawnedFlash = false;
    private final SoundType soundType;

    @SuppressWarnings("unchecked")
    public KiBlastAddon(Level level, LivingEntity owner, SoundType soundType) {
        super((EntityType) MainEntities.KI_BLAST.get(), level);
        this.setOwner(owner);
        this.soundType = soundType;
        this.setNoGravity(true);

        if (owner instanceof net.minecraft.world.entity.player.Player player) {
            net.minecraft.sounds.SoundEvent sound = switch (soundType) {
                case DEATH_BALL -> ModSounds.DEATHBALL_FIRE.get();
                case BIG_BANG   -> ModSounds.BIGBANG_FIRE.get();
                default         -> ModSounds.BLAST.get();
            };
            level.playSound(player, owner.getX(), owner.getY(), owner.getZ(),
                    sound, SoundSource.PLAYERS, 1.0f,
                    0.9f + level.random.nextFloat() * 0.2f);
        }
    }

    // Required constructor for entity type deserialization
    public KiBlastAddon(EntityType<? extends AbstractKiProjectile> type, Level level) {
        super(type, level);
        this.soundType = SoundType.GENERIC;
    }
}
