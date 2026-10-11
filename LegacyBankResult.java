public class LegacyBankResult {
    private final boolean success;
    private final String reference;

    public LegacyBankResult(boolean success, String reference) {
        this.success = success;
        this.reference = reference;
    }

    public boolean isSuccess() { return success; }
    public String getReference() { return reference; }
}
