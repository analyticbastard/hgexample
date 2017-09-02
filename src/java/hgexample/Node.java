package hgexample;

/**
 * Created by javier on 17/09/2017.
 */
public class Node {
    private Pk id;
    private IHashgraph hg;

    public Node(Pk id, IHashgraph hg) {
        this.id = id;
        this.hg = hg;
    }

    public IHashgraph getHg() {
        return hg;
    }

    public Pk getId() {
        return id;
    }
}
