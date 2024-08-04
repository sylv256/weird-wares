package gay.sylv.weird_wares.impl.network.server;

import gay.sylv.weird_wares.impl.DataAttachments;
import gay.sylv.weird_wares.impl.network.client.RequestGlintSyncPayload;
import gay.sylv.weird_wares.impl.util.Initializable;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Handles packets received on the server-side.
 */
public final class ServerPackets implements Initializable {
	public static final ServerPackets INSTANCE = new ServerPackets();
	
	private ServerPackets() {}
	
	@Override
	public void initialize() {
		ServerPlayNetworking.registerGlobalReceiver(RequestGlintSyncPayload.TYPE, (payload, context) -> {
			//noinspection resource
			DataAttachments
					.getGlintOptional(context.player().level().getChunk(payload.chunkPos().x, payload.chunkPos().z))
					.filter(glints -> !glints.isEmpty())
					.ifPresent(glints -> ServerPlayNetworking.send(context.player(), new SyncGlintPayload(payload.chunkPos(), glints)));
		});
	}
}
