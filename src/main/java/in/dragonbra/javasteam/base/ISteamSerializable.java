package in.dragonbra.javasteam.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author lngtr
 * @since 2018-02-21
 */
public interface ISteamSerializable {

    void serialize(OutputStream stream) throws IOException;

    void deserialize(InputStream stream) throws IOException;
}
