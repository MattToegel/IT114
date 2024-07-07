package Project.Common;

public class EnergyPayload extends XYPayload{
    private int energy;
    public EnergyPayload(int x, int y){
        super(x, y);
    }
    public EnergyPayload(){
        setPayloadType(PayloadType.ENERGY);
    }
    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }
    @Override
    public String toString(){
        return super.toString() + " Energy: " + getEnergy();
    }
}
