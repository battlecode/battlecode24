package battlecode.instrumenter.sample.instrumentertest;

@SuppressWarnings("unused")
public class DoesntOverrideToString {
    public String getToString() {
        return this.toString();
    }
}
