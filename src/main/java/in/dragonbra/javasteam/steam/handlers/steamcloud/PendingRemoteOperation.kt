package `in`.dragonbra.javasteam.steam.handlers.steamcloud

import `in`.dragonbra.javasteam.enums.EOSType
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientObjects.CCloud_PendingRemoteOperation
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientObjects.ECloudPendingRemoteOperation

class PendingRemoteOperation(operation: CCloud_PendingRemoteOperation) {
    val operation: ECloudPendingRemoteOperation = operation.operation
    val machineName: String = operation.machineName
    val clientId: Long = operation.clientId
    val timeLastUpdated: Int = operation.timeLastUpdated
    val osType: EOSType = EOSType.from(operation.osType)
    val deviceType: Int = operation.deviceType
}
