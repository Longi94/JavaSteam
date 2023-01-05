package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient.*;
import in.dragonbra.javasteam.types.JobID;

public interface IContentServerDirectory {

    /* CContentServerDirectory_GetServersForSteamPipe_Response */
    JobID GetServersForSteamPipe(CContentServerDirectory_GetServersForSteamPipe_Request request);

    /* CContentServerDirectory_GetDepotPatchInfo_Response */
    JobID GetDepotPatchInfo(CContentServerDirectory_GetDepotPatchInfo_Request request);

    /* CContentServerDirectory_GetClientUpdateHosts_Response */
    JobID GetClientUpdateHosts(CContentServerDirectory_GetClientUpdateHosts_Request request);

    /* CContentServerDirectory_GetManifestRequestCode_Response */
    JobID GetManifestRequestCode(CContentServerDirectory_GetManifestRequestCode_Request request);
}
