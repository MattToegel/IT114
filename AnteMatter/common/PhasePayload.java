package AnteMatter.common;

public class PhasePayload extends Payload{
    private Phase phase;
    public PhasePayload(){
        setPayloadType(PayloadType.PHASE);
    }
    /**
     * @return the phase
     */
    public Phase getPhase() {
        return phase;
    }

    /**
     * @param phase the phase to set
     */
    public void setPhase(Phase phase) {
        this.phase = phase;
    }
    @Override
    public String toString(){
        return String.format("%s", phase.name());
    }
}
