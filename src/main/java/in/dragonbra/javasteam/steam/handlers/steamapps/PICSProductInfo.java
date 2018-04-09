package in.dragonbra.javasteam.steam.handlers.steamapps;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientPICSProductInfoResponse;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.KeyValue;
import in.dragonbra.javasteam.util.Strings;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.MemoryStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

/**
 * Represents the information for a single app or package
 */
public class PICSProductInfo extends CallbackMsg {

    private int id;

    private int changeNumber;

    private boolean missingToken;

    private byte[] shaHash;

    private KeyValue keyValues;

    private boolean onlyPublic;

    private boolean useHttp;

    private URI httpUri;

    public PICSProductInfo(CMsgClientPICSProductInfoResponse.Builder parentResponse, CMsgClientPICSProductInfoResponse.AppInfo appInfo) {
        id = appInfo.getAppid();
        changeNumber = appInfo.getChangeNumber();
        missingToken = appInfo.getMissingToken();
        shaHash = appInfo.getSha().toByteArray();

        keyValues = new KeyValue();
        if (appInfo.hasBuffer() && !appInfo.getBuffer().isEmpty()) {
            // we don't want to read the trailing null byte
            try (MemoryStream ms = new MemoryStream(appInfo.getBuffer().toByteArray(), 0, appInfo.getBuffer().size() - 1)) {
                keyValues.readAsText(ms);
            } catch (IOException e) {
                throw new IllegalArgumentException("failed to read buffer", e);
            }
        }

        onlyPublic = appInfo.getOnlyPublic();

        // We should have all these fields set for the response to a metadata-only request, but guard here just in case.
        if (shaHash != null && shaHash.length > 0 && !Strings.isNullOrEmpty(parentResponse.getHttpHost())) {
            String shaString = Strings.toHex(shaHash).replace("-", "").toLowerCase();
            String uriString = String.format("http://%s/appinfo/%d/sha/%s.txt.gz", parentResponse.getHttpHost(), id, shaString);
            httpUri = URI.create(uriString);
        }

        useHttp = httpUri != null && appInfo.getSize() >= parentResponse.getHttpMinSize();
    }

    public PICSProductInfo(CMsgClientPICSProductInfoResponse.PackageInfo packageInfo) {
        id = packageInfo.getPackageid();
        changeNumber = packageInfo.getChangeNumber();
        missingToken = packageInfo.getMissingToken();
        shaHash = packageInfo.getSha().toByteArray();

        keyValues = new KeyValue();
        if (packageInfo.hasBuffer()) {
            // we don't want to read the trailing null byte
            try (BinaryReader br = new BinaryReader(new ByteArrayInputStream(packageInfo.getBuffer().toByteArray()))) {
                // steamclient checks this value == 1 before it attempts to read the KV from the buffer
                // see: CPackageInfo::UpdateFromBuffer(CSHA const&,uint,CUtlBuffer &)
                // todo: we've apparently ignored this with zero ill effects, but perhaps we want to respect it?
                br.readInt();

                keyValues.tryReadAsBinary(br);
            } catch (IOException e) {
                throw new IllegalArgumentException("failed to read buffer", e);
            }
        }
    }

    public int getId() {
        return id;
    }

    public int getChangeNumber() {
        return changeNumber;
    }

    public boolean isMissingToken() {
        return missingToken;
    }

    public byte[] getShaHash() {
        return shaHash;
    }

    public KeyValue getKeyValues() {
        return keyValues;
    }

    public boolean isOnlyPublic() {
        return onlyPublic;
    }

    public boolean isUseHttp() {
        return useHttp;
    }

    public URI getHttpUri() {
        return httpUri;
    }
}
