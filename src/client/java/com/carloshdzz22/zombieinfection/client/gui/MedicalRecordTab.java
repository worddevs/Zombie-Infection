package com.carloshdzz22.zombieinfection.client.gui;

public enum MedicalRecordTab {
    STATUS("screen.zombie-infection.record.tab.status"),
    TREATMENT("screen.zombie-infection.record.tab.treatment"),
    OUTBREAK("screen.zombie-infection.record.tab.outbreak");

    private final String translationKey;

    MedicalRecordTab(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }
}
