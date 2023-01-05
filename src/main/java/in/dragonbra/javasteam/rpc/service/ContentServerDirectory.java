package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesContentsystemSteamclient;
import in.dragonbra.javasteam.rpc.IContentServerDirectory;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.types.JobID;

// TODO implement

@SuppressWarnings("unused")
public class ContentServerDirectory extends UnifiedService implements IContentServerDirectory {

    public ContentServerDirectory(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public JobID GetServersForSteamPipe(SteammessagesContentsystemSteamclient.CContentServerDirectory_GetServersForSteamPipe_Request request) {
        return null;
    }

    @Override
    public JobID GetDepotPatchInfo(SteammessagesContentsystemSteamclient.CContentServerDirectory_GetDepotPatchInfo_Request request) {
        return null;
    }

    @Override
    public JobID GetClientUpdateHosts(SteammessagesContentsystemSteamclient.CContentServerDirectory_GetClientUpdateHosts_Request request) {
        return null;
    }

    @Override
    public JobID GetManifestRequestCode(SteammessagesContentsystemSteamclient.CContentServerDirectory_GetManifestRequestCode_Request request) {
        return null;
    }
}
