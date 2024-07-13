package Project.Client.Interfaces;

public interface IEnergyEvents extends IGameEvents {
    /**
     * The energy value of a specific id
     * 
     * @param clientId
     * @param energy
     */
    void onUpdateEnergy(long clientId, int energy);
}
