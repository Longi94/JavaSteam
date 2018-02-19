package in.dragonbra.javasteam.enums;

import java.util.Arrays;

public enum EClientStatAggregateMethod {

    LatestOnly(0),
    Sum(1),
    Event(2),
    Scalar(3),

    ;

    private final int code;

    EClientStatAggregateMethod(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public EClientStatAggregateMethod from(int code) {
        return Arrays.stream(EClientStatAggregateMethod.values()).filter(x -> x.code == code).findFirst().orElse(null);
    }
}
