package com.sebastian_eggers.MediApp.Enum;

public enum DrugForm {
    PILL("pill"),
    POWDER("powder"),
    GRANULATE("granulate"),
    DROPS("drops"),
    JUICE("juice"),
    SYRUP("syrup"),
    INFUSION("infusion"),
    INJECTION("injection"),
    OINTMENT("ointment"),
    CREAM("cream");

    private String name;

    DrugForm(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static DrugForm translate(String value) {
        switch (value) {
            case "Puder":
                return POWDER;
            case "Granulat":
                return GRANULATE;
            case "Tropfen":
                return DROPS;
            case "Saft":
                return JUICE;
            case "Sirup":
                return SYRUP;
            case "Infusion":
                return INFUSION;
            case "Injektion":
                return INJECTION;
            case "Salbe":
                return OINTMENT;
            case "Creme":
                return  CREAM;
            default:
                return PILL;
        }
    }
}
