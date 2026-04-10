package quality.valid;

import java.util.Objects;

public class DuplicateMethodBodyValid {

    private String name;
    private int version;

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DuplicateMethodBodyValid)) {
            return false;
        }
        DuplicateMethodBodyValid that = (DuplicateMethodBodyValid) other;
        return version == that.version && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }

    public int differentMethodCall(int a, int b) {
        int total = a + b;
        total = flipSign(total);
        return total;
    }

    public int differentOperator(int a, int b) {
        int total = a - b;
        total = total * 2;
        return total;
    }

    public int differentLiteral(int a, int b) {
        int total = a + b;
        total = total * 2;
        return total;
    }

    public int useFieldRef(int a, int b) {
        int total = a + b;
        total = total + this.version;
        return total;
    }

    public int useOtherFieldRef(int a, int b) {
        int total = a + b;
        total = total + this.other;
        return total;
    }

    private int other;

    private int flipSign(int x) {
        return -x;
    }
}
