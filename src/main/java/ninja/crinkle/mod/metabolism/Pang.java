package ninja.crinkle.mod.metabolism;

public enum Pang {
    None,
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

    public static Pang from(Metabolism metabolism) {
        if (metabolism.currentTraining() > metabolism.intensity())
            return Pang.Minor;
        else if (metabolism.currentTraining() <= metabolism.intensity())
            return Pang.Major;
        return Pang.None;
    }
}
