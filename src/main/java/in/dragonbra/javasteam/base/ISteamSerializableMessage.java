package in.dragonbra.javasteam.base;

import in.dragonbra.javasteam.enums.EMsg;

/**
 * @author lngtr
 * @since 2018-02-21
 */
public interface ISteamSerializableMessage extends ISteamSerializable {
    EMsg getEMsg();
}
