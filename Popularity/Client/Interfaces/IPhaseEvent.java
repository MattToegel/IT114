package Popularity.Client.Interfaces;

import Popularity.Common.Phase;

public interface IPhaseEvent extends IGameEvents {
    /**
     * Receives the current phase
     * 
     * @param phase
     */
    void onReceivePhase(Phase phase);
}