@echo off

SET dst=../../src/main/java/

echo Steam Messages Base
protoc steammessages_base.proto --java_out=%dst%
echo.
echo.

echo Encrypted App Ticket
protoc encrypted_app_ticket.proto --java_out=%dst%
echo.
echo.

echo Steam Messages ClientServer
protoc steammessages_clientserver.proto --java_out=%dst%
:: ..\..\Protogen\protogen -i:"steammessages_clientserver_2.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\SteamMsgClientServer2.cs" -t:csharp -ns:"SteamKit2.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -i:"steammessages_clientserver_friends.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\SteamMsgClientServerFriends.cs" -t:csharp -ns:"SteamKit2.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -i:"steammessages_clientserver_login.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\SteamMsgClientServerLogin.cs" -t:csharp -ns:"SteamKit2.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -i:"steammessages_sitelicenseclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\SteamMsgSiteLicenseClient.cs" -t:csharp -ns:"SteamKit2.Internal" -p:detectMissing
echo.
echo.

echo Content Manifest
:: ..\..\Protogen\protogen -i:"content_manifest.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\ContentManifest.cs" -t:csharp -ns:"SteamKit2.Internal" -p:detectMissing
echo.
echo.

echo Unified Messages
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_unified_base.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgUnifiedBase.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing

:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_broadcast.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgBroadcast.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_cloud.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgCloud.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_credentials.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgCredentials.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_datapublisher.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgDataPublisher.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_depotbuilder.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgDepotBuilder.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_deviceauth.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgDeviceAuth.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_econ.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgEcon.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_gamenotifications.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgGameNotifications.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_gameservers.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgGameServers.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_linkfilter.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgLinkFilter.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_inventory.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgInventory.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_offline.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgOffline.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_parental.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgParental.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_partnerapps.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgPartnerApps.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_physicalgoods.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgPhysicalGoods.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_player.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgPlayer.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_publishedfile.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgPublishedFile.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_secrets.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgSecrets.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_site_license.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgSiteLicense.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_twofactor.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgTwoFactor.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_useraccount.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgUserAccount.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing
:: ..\..\Protogen\protogen -s:..\ -i:"steammessages_video.steamclient.proto" -o:"..\..\..\SteamKit2\SteamKit2\Base\Generated\Unified\SteamMsgVideo.cs" -t:csharp -ns:"SteamKit2.Unified.Internal" -p:detectMissing

pause
