package in.dragonbra.javasteam.base;

import in.dragonbra.javasteam.enums.EMsg;

/**
 * @author lngtr
 * @since 2018-02-21
 */
public interface ISteamSerializableHeader extends ISteamSerializable {
    void setEMsg(EMsg msg);
}
