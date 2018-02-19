package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EServerType {

    Invalid(-1),
    First(0),
    Shell(0),
    GM(1),
    AM(3),
    BS(4),
    VS(5),
    ATS(6),
    CM(7),
    FBS(8),
    BoxMonitor(9),
    SS(10),
    DRMS(11),
    Console(13),
    PICS(14),
    Client(15),
    DP(17),
    WG(18),
    SM(19),
    SLC(20),
    UFS(21),
    Util(23),
    Community(24),
    AppInformation(26),
    Spare(27),
    FTS(28),
    PS(30),
    IS(31),
    CCS(32),
    DFS(33),
    LBS(34),
    MDS(35),
    CS(36),
    GC(37),
    NS(38),
    OGS(39),
    WebAPI(40),
    UDS(41),
    MMS(42),
    GMS(43),
    KGS(44),
    UCM(45),
    RM(46),
    FS(47),
    Econ(48),
    Backpack(49),
    UGS(50),
    StoreFeature(51),
    MoneyStats(52),
    CRE(53),
    UMQ(54),
    Workshop(55),
    BRP(56),
    GCH(57),
    MPAS(58),
    Trade(59),
    Secrets(60),
    Logsink(61),
    Market(62),
    Quest(63),
    WDS(64),
    ACS(65),
    PNP(66),
    TaxForm(67),
    ExternalMonitor(68),
    Parental(69),
    PartnerUpload(70),
    Partner(71),
    ES(72),
    DepotWebContent(73),
    ExternalConfig(74),
    GameNotifications(75),
    MarketRepl(76),
    MarketSearch(77),
    Localization(78),
    Steam2Emulator(79),
    PublicTest(80),
    SolrMgr(81),
    BroadcastRelay(82),
    BroadcastDirectory(83),
    VideoManager(84),
    TradeOffer(85),
    BroadcastChat(86),
    Phone(87),
    AccountScore(88),
    Support(89),
    LogRequest(90),
    LogWorker(91),
    EmailDelivery(92),
    InventoryManagement(93),
    Auth(94),
    StoreCatalog(95),
    HLTVRelay(96),
    Max(97),

    ;

    private final int code;

    EServerType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public EServerType from(int code) {
        return Arrays.stream(EServerType.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
