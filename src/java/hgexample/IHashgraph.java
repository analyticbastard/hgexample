package hgexample;

import java.util.Map;

/**
 * Created by javier on 17/09/2017.
 */
public interface IHashgraph {
    Event head(Pk node);

    Map<Node, Event> roundWitnesses(long round);

    Map<Node, Event> lastFamousWitnesses();

    long currentRound();

    void insertEvent(Event event);
}
