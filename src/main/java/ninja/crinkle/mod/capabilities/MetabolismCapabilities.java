package ninja.crinkle.mod.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class MetabolismCapabilities {
    public static final Capability<WetCapability> WET = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<MessCapability> MESS = CapabilityManager.get(new CapabilityToken<>() {
    });

    private MetabolismCapabilities() {
    }
}
