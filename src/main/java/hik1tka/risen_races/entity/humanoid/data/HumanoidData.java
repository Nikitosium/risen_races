package hik1tka.risen_races.entity.humanoid.data;

public class HumanoidData {
    private final String raceId;
    private final boolean isFemale;
    private final String profession;
    private final int level;

    public HumanoidData(String raceId, boolean isFemale, String profession, int level) {
        this.raceId = raceId;
        this.isFemale = isFemale;
        this.profession = profession;
        this.level = level;
    }

    public String getRaceId() { return raceId; }
    public boolean isFemale() { return isFemale; }
    public String getProfession() { return profession; }
    public int getLevel() { return level; }
}