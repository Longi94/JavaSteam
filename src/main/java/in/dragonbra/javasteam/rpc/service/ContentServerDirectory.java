package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient.*;
import in.dragonbra.javasteam.rpc.IContentServerDirectory;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public class ContentServerDirectory extends UnifiedService implements IContentServerDirectory {

    public ContentServerDirectory(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public JobID GetServersForSteamPipe(CContentServerDirectory_GetServersForSteamPipe_Request request) {
        return sendMessage(CContentServerDirectory_GetServersForSteamPipe_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetDepotPatchInfo(CContentServerDirectory_GetDepotPatchInfo_Request request) {
        return sendMessage(CContentServerDirectory_GetDepotPatchInfo_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetClientUpdateHosts(CContentServerDirectory_GetClientUpdateHosts_Request request) {
        return sendMessage(CContentServerDirectory_GetClientUpdateHosts_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetManifestRequestCode(CContentServerDirectory_GetManifestRequestCode_Request request) {
        return sendMessage(CContentServerDirectory_GetManifestRequestCode_Request.class, request.toBuilder());
    }
}
