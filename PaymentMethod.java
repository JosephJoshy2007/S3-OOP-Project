// Module 4: Abstract PaymentMethod Strategy
public abstract class PaymentMethod {
    private final String methodName;

    public PaymentMethod(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public abstract boolean executeTransaction(double amount) throws PaymentProcessingException;

    @Override
    public String toString() {
        return methodName;
    }
}
