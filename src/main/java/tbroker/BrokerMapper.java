package tbroker;

interface BrokerMapper {
    Broker getBroker(String token);

    boolean isSymValid(String token);
}
