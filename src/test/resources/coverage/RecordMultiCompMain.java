package test;
public record RecordMultiComp(String name, int value, boolean active) {
    public void customAction() {}
    public String name() { return name; }
    public int value() { return value; }
    public boolean active() { return active; }
}
