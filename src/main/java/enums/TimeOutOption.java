package enums;

public enum TimeOutOption {
    LESS() {
        @Override
        public boolean isTimeoutConditionMet(long millis, long actualDelay) {
            return actualDelay <= millis;
        }
    },
    MORE() {
        @Override
        public boolean isTimeoutConditionMet(long millis, long actualDelay) {
            return actualDelay >= millis;
        }
    };

    public abstract boolean isTimeoutConditionMet(long millis, long actualDelay);
}
