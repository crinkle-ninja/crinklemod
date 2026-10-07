package ninja.crinkle.mod.events.handlers;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import ninja.crinkle.mod.events.PangEvent;
import ninja.crinkle.mod.metabolism.Pang;
import ninja.crinkle.mod.undergarment.Undergarment;

public class PangEventHandler {
    @SubscribeEvent
    public void onAccidentStart(PangEvent.Start event) {
        if (event.pang() != Pang.Accident) return;

        // Undergarments
        Undergarment undergarment = Undergarment.of(event.player());
        if (undergarment == Undergarment.EMPTY) return;
        switch(event.type()) {
            case Wet -> undergarment.setLiquids(undergarment.getLiquids() + 1);
            case Mess -> undergarment.setSolids(undergarment.getSolids() + 1);
        }
        undergarment.syncServer();

        // Chat Text
        // TODO: messages
    }
}
