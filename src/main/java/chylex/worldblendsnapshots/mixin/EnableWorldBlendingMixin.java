package chylex.worldblendsnapshots.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Optional;
import java.util.function.Supplier;

@Mixin(ChunkStorage.class)
public abstract class EnableWorldBlendingMixin {
	private static final int VERSION_1_18_1 = 2865;
	
	private static final String BLENDING_DATA_TAG = "blending_data";
	private static final String OLD_NOISE_TAG = "old_noise";
	private static final String STATUS_TAG = "Status";
	
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@Inject(method = "upgradeChunkTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putInt(Ljava/lang/String;I)V"))
	private void addBlendingTag(ResourceKey<Level> world, Supplier<DimensionDataStorage> storage, CompoundTag tag, Optional<ResourceKey<Codec<? extends ChunkGenerator>>> key, CallbackInfoReturnable<CompoundTag> cir) {
		final int version = ChunkStorage.getVersion(tag);
		
		if (version < VERSION_1_18_1 && !tag.contains(BLENDING_DATA_TAG) && ChunkStatus.byName(tag.getString(STATUS_TAG)).isOrAfter(ChunkStatus.BIOMES)) {
			CompoundTag bd = new CompoundTag();
			bd.putBoolean(OLD_NOISE_TAG, true);
			tag.put(BLENDING_DATA_TAG, bd);
		}
	}
	
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@Inject(method = "upgradeChunkTag", at = @At("RETURN"))
	private void removeBrokenBlendingTag(ResourceKey<Level> world, Supplier<DimensionDataStorage> storage, CompoundTag tag, Optional<ResourceKey<Codec<? extends ChunkGenerator>>> key, CallbackInfoReturnable<CompoundTag> cir) {
		final int version = ChunkStorage.getVersion(tag);
		
		if (version >= VERSION_1_18_1 && tag.contains(BLENDING_DATA_TAG) && !ChunkStatus.byName(tag.getString(STATUS_TAG)).isOrAfter(ChunkStatus.BIOMES)) {
			tag.remove(BLENDING_DATA_TAG);
		}
	}
}
