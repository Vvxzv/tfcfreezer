package com.github.Mev10.common.capabilities;

import net.minecraftforge.energy.EnergyStorage;

public class InventoryConsumerEnergyStorage extends EnergyStorage {
    private final EnergyStorageCallback callback;
    public InventoryConsumerEnergyStorage(int capacity, int maxTransfer, EnergyStorageCallback callback) {
        super(capacity, maxTransfer);
        this.callback = callback;
    }

    @Override
    public int extractEnergy(int amount, boolean simulate) {
        int consumed = super.extractEnergy(amount, simulate);
        if(consumed > 0 && !simulate){
            callback.energyLevelChanged();
        }
        return consumed;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if(received > 0 && !simulate){
            callback.energyLevelChanged();
        }
        return received;
    }
}
