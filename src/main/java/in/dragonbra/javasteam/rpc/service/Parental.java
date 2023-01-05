package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesParentalSteamclient;
import in.dragonbra.javasteam.rpc.IParental;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.types.JobID;

// TODO implement

@SuppressWarnings("unused")
public class Parental extends UnifiedService implements IParental {

    public Parental(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public JobID EnableParentalSettings(SteammessagesParentalSteamclient.CParental_EnableParentalSettings_Request request) {
        return null;
    }

    @Override
    public JobID DisableParentalSettings(SteammessagesParentalSteamclient.CParental_DisableParentalSettings_Request request) {
        return null;
    }

    @Override
    public JobID GetParentalSettings(SteammessagesParentalSteamclient.CParental_GetParentalSettings_Request request) {
        return null;
    }

    @Override
    public JobID GetSignedParentalSettings(SteammessagesParentalSteamclient.CParental_GetSignedParentalSettings_Request request) {
        return null;
    }

    @Override
    public JobID SetParentalSettings(SteammessagesParentalSteamclient.CParental_SetParentalSettings_Request request) {
        return null;
    }

    @Override
    public JobID ValidateToken(SteammessagesParentalSteamclient.CParental_ValidateToken_Request request) {
        return null;
    }

    @Override
    public JobID ValidatePassword(SteammessagesParentalSteamclient.CParental_ValidatePassword_Request request) {
        return null;
    }

    @Override
    public JobID LockClient(SteammessagesParentalSteamclient.CParental_LockClient_Request request) {
        return null;
    }

    @Override
    public JobID RequestRecoveryCode(SteammessagesParentalSteamclient.CParental_RequestRecoveryCode_Request request) {
        return null;
    }

    @Override
    public JobID DisableWithRecoveryCode(SteammessagesParentalSteamclient.CParental_DisableWithRecoveryCode_Request request) {
        return null;
    }
}
