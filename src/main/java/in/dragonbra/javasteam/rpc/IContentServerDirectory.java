package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient.*;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public interface IContentServerDirectory {

    /* CContentServerDirectory_GetServersForSteamPipe_Response */
    AsyncJobSingle<ServiceMethodResponse> GetServersForSteamPipe(CContentServerDirectory_GetServersForSteamPipe_Request request);

    /* CContentServerDirectory_GetDepotPatchInfo_Response */
    AsyncJobSingle<ServiceMethodResponse> GetDepotPatchInfo(CContentServerDirectory_GetDepotPatchInfo_Request request);

    /* CContentServerDirectory_GetClientUpdateHosts_Response */
    AsyncJobSingle<ServiceMethodResponse> GetClientUpdateHosts(CContentServerDirectory_GetClientUpdateHosts_Request request);

    /* CContentServerDirectory_GetManifestRequestCode_Response */
    AsyncJobSingle<ServiceMethodResponse> GetManifestRequestCode(CContentServerDirectory_GetManifestRequestCode_Request request);
}
