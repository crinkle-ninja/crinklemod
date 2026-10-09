package ninja.crinkle.mod.metabolism;

public enum Pang {
    None,
    Delay,
    Relief,
    Minor,
    Major,
    Accident,
    ;

    public static Pang from(String stringValue) {
        if (stringValue.isBlank()) {
            return None;
        }
        return Pang.valueOf(Pang.class, stringValue);
    }
}
