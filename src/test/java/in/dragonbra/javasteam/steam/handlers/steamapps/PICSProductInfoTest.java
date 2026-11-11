package in.dragonbra.javasteam.steam.handlers.steamapps;

import com.google.protobuf.ByteString;
import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverAppinfo.CMsgClientPICSProductInfoResponse;
import in.dragonbra.javasteam.types.KeyValue;
import in.dragonbra.javasteam.util.stream.MemoryStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class PICSProductInfoTest extends TestBase {

    private static final String APP_INFO_TEXT = "\"appinfo\"\n{\n\t\"appid\"\t\t\"440\"\n}";

    private static CMsgClientPICSProductInfoResponse.AppInfo.Builder appInfo(byte[] buffer) {
        return CMsgClientPICSProductInfoResponse.AppInfo.newBuilder()
                .setAppid(440)
                .setChangeNumber(1)
                .setBuffer(ByteString.copyFrom(buffer));
    }

    private static byte[] packageBuffer(int format) throws IOException {
        var kv = new KeyValue("440");
        kv.getChildren().add(new KeyValue("billingtype", "10"));

        try (var ms = new MemoryStream()) {
            kv.saveToStream(ms.asOutputStream(), true);
            var kvBytes = ms.toByteArray();

            return ByteBuffer.allocate(Integer.BYTES + kvBytes.length)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .putInt(format)
                    .put(kvBytes)
                    .array();
        }
    }

    private static PICSProductInfo packageInfo(byte[] buffer) {
        var packageInfo = CMsgClientPICSProductInfoResponse.PackageInfo.newBuilder()
                .setPackageid(440)
                .setChangeNumber(1)
                .setBuffer(ByteString.copyFrom(buffer))
                .build();

        return new PICSProductInfo(CMsgClientPICSProductInfoResponse.newBuilder(), packageInfo);
    }

    @Test
    public void appTextBufferWithTrailingNullIsRead() {
        var buffer = (APP_INFO_TEXT + "\0").getBytes(StandardCharsets.UTF_8);

        var info = new PICSProductInfo(CMsgClientPICSProductInfoResponse.newBuilder(), appInfo(buffer).build());

        Assertions.assertEquals("440", info.getKeyValues().get("appid").asString());
    }

    @Test
    public void appTextBufferWithoutTrailingNullIsRead() {
        var buffer = APP_INFO_TEXT.getBytes(StandardCharsets.UTF_8);

        var info = new PICSProductInfo(CMsgClientPICSProductInfoResponse.newBuilder(), appInfo(buffer).build());

        Assertions.assertEquals("440", info.getKeyValues().get("appid").asString());
    }

    @Test
    public void useHttpRequiresMetaDataOnlyAndMinimumSize() {
        var app = CMsgClientPICSProductInfoResponse.AppInfo.newBuilder()
                .setAppid(440)
                .setSha(ByteString.copyFrom(new byte[20]))
                .setSize(200)
                .build();

        var available = CMsgClientPICSProductInfoResponse.newBuilder()
                .setMetaDataOnly(true)
                .setHttpHost("example.invalid")
                .setHttpMinSize(100);
        Assertions.assertTrue(new PICSProductInfo(available, app).isUseHttp());

        // a minimum size of zero means HTTP is not available for this response
        var zeroMinSize = available.clone().setHttpMinSize(0);
        Assertions.assertFalse(new PICSProductInfo(zeroMinSize, app).isUseHttp());

        var notMetaDataOnly = available.clone().setMetaDataOnly(false);
        Assertions.assertFalse(new PICSProductInfo(notMetaDataOnly, app).isUseHttp());

        var tooSmall = available.clone().setHttpMinSize(500);
        Assertions.assertFalse(new PICSProductInfo(tooSmall, app).isUseHttp());
    }

    @Test
    public void packageBufferWithKnownFormatIsRead() throws IOException {
        var info = packageInfo(packageBuffer(1));

        Assertions.assertEquals("10", info.getKeyValues().get("billingtype").asString());
    }

    @Test
    public void packageBufferWithUnknownFormatIsIgnored() throws IOException {
        var info = packageInfo(packageBuffer(2));

        Assertions.assertTrue(info.getKeyValues().getChildren().isEmpty());
    }

    @Test
    public void truncatedPackageBufferIsIgnored() {
        var info = packageInfo(new byte[]{1, 0});

        Assertions.assertTrue(info.getKeyValues().getChildren().isEmpty());
    }
}
