package gay.sylv.weird_wares.impl.network.server;

import gay.sylv.weird_wares.impl.network.Networking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static gay.sylv.weird_wares.impl.util.Constants.modId;

public record SyncGlintPayload(ChunkPos chunkPos, List<BlockPos> glints) implements CustomPacketPayload {
	public static final Type<SyncGlintPayload> TYPE = new Type<>(modId("sync_glint"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SyncGlintPayload> CODEC = StreamCodec.composite(
			Networking.Codecs.CHUNK_POS, SyncGlintPayload::chunkPos,
			Networking.Codecs.POS_LIST, SyncGlintPayload::glints,
			SyncGlintPayload::new
	);
	
	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
