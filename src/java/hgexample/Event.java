package hgexample;

import clojure.lang.IPersistentSet;

/**
 * Created by javier on 17/09/2017.
 */
public abstract class Event {
    private IHashgraph hg;

    public Event(IHashgraph hg) {
        this.hg = hg;
    }

    abstract boolean canSee(Event ancestor);

    abstract boolean canStronglySee(Event ancestor);

    abstract IPersistentSet canStronglySeeAll();

    abstract Event makeWitness();
}
