package in.dragonbra.javasteam.steam.handlers.steamapps;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSProductInfoResponse;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.KeyValue;
import in.dragonbra.javasteam.util.Strings;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.MemoryStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
            try {
                // get the buffer as a string using the jvm's default charset.
                // note: IDK why, but we have to encode this using the default charset
                String bufferString = appInfo.getBuffer().toString(Charset.defaultCharset());
                // get the buffer as a byte array using utf-8 as a supported charset
                byte[] byteBuffer = bufferString.getBytes(StandardCharsets.UTF_8);
                // we don't want to read the trailing null byte
                MemoryStream ms = new MemoryStream(byteBuffer, 0, byteBuffer.length - 1);
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

    /**
     * @return the ID of the app or package.
     */
    public int getId() {
        return id;
    }

    /**
     * @return the current change number for the app or package.
     */
    public int getChangeNumber() {
        return changeNumber;
    }

    /**
     * @return if an access token was required for the request.
     */
    public boolean isMissingToken() {
        return missingToken;
    }

    /**
     * @return the hash of the content.
     */
    public byte[] getShaHash() {
        return shaHash;
    }

    /**
     * @return the KeyValue info.
     */
    public KeyValue getKeyValues() {
        return keyValues;
    }

    /**
     * @return for an app request, returns if only the public information was requested.
     */
    public boolean isOnlyPublic() {
        return onlyPublic;
    }

    /**
     * @return whether or not to use HTTP to load the KeyValues data.
     */
    public boolean isUseHttp() {
        return useHttp;
    }

    /**
     * @return for an app metadata-only request, returns the Uri for HTTP appinfo requests.
     */
    public URI getHttpUri() {
        return httpUri;
    }
}
