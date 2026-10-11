public class LegacyBankGatewayApi {
    public LegacyBankResult chargeAccount(String bankAccountNumber, double amount) {
        if (bankAccountNumber != null && !bankAccountNumber.trim().isEmpty() && amount > 0) {
            return new LegacyBankResult(true, "LEGACY-AUTH-" + System.currentTimeMillis());
        }
        return new LegacyBankResult(false, "ERR-INVALID-ACC");
    }
}
