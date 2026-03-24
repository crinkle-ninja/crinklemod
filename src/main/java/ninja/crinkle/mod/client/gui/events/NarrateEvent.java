package ninja.crinkle.mod.client.gui.events;

import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.NotNull;

public class NarrateEvent extends AbstractEvent {
    public static final Key<NarrateEvent> KEY = new Key<>();
    private final NarrationElementOutput narrationElementOutput;

    public NarrateEvent(EventNode source, @NotNull NarrationElementOutput pNarrationElementOutput) {
        super(KEY, source);
        this.narrationElementOutput = pNarrationElementOutput;
    }

    public NarrationElementOutput narrationElementOutput() {
        return narrationElementOutput;
    }

    @Override
    public String toString() {
        return "NarrateEvent{" +
                "narrationElementOutput=" + narrationElementOutput +
                ", " + super.toString() +
                '}';
    }
}