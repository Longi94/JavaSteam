package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient.*;
import in.dragonbra.javasteam.rpc.interfaces.IContentServerDirectory;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

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
    public AsyncJobSingle<ServiceMethodResponse> GetServersForSteamPipe(CContentServerDirectory_GetServersForSteamPipe_Request request) {
        return sendMessage(request, "GetServersForSteamPipe");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetDepotPatchInfo(CContentServerDirectory_GetDepotPatchInfo_Request request) {
        return sendMessage(request, "GetDepotPatchInfo");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetClientUpdateHosts(CContentServerDirectory_GetClientUpdateHosts_Request request) {
        return sendMessage(request, "GetClientUpdateHosts");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetManifestRequestCode(CContentServerDirectory_GetManifestRequestCode_Request request) {
        return sendMessage(request, "GetManifestRequestCode");
    }
}
