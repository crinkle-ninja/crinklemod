package ninja.crinkle.mod.metabolism;

import ninja.crinkle.mod.capabilities.IMetabolism;

public enum Pang {
    None,
    Minor,
    Major,
    Accident;

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
