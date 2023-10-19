package battlecode.common;

/**
 * Enumerates the different skill types a unit can specialize in.
 */

public enum SkillType{

    // Exp req levels 0-6, specialization effects cooldown cost change levels 0-6, specialization effect skill change (%), jailed penalty level 0-6
    /**
     * Attacking immediately reduces one enemy unit's (in range) base health by 15 (level 0), with a cooldown cost of 20.
     *
     * @battlecode.doc.skilltype
     */
    ATTACK(0, 25, 50, 75, 125, 175, 250, 0, -1, -2, -3, -3, -6, -10, 0, 5, 10, 15, 20, 30, 50, -1, -5, -5, -10, -10, -15, -15),
    
    /**
     * Healing adds 8 health points to a nearby friendly unit, with a cooldown cost of 30. (level 0)
     *
     * @battlecode.doc.skilltype
     */
    BUILD(0, 10, 20, 30, 50, 75, 125, 0, -5, -10, -15, -20, -30, -50, 0, 0, 0, 0, 0, 0, 0, -1, -2, -2, -5, -5, -10, -10),
    
    /**
     * Build uses team resources to dig, fill, or build a trap.
     *
     * @battlecode.doc.skilltype
     */
    HEAL(0, 10, 20, 30,50, 75, 125, 0, -5, -10, -15, -15, -15, -25, 0, 3, 5, 7, 10, 15, 25, -1, -2, -2, -5, -5, -10, -10), ;

    public final int experienceReq0;
    public final int experienceReq1;
    public final int experienceReq2;
    public final int experienceReq3;
    public final int experienceReq4;
    public final int experienceReq5;
    public final int experienceReq6;

    public final int cooldown0;
    public final int cooldown1;
    public final int cooldown2;
    public final int cooldown3;
    public final int cooldown4;
    public final int cooldown5;
    public final int cooldown6;

    public final int skillEffect0;
    public final int skillEffect1;
    public final int skillEffect2;
    public final int skillEffect3;
    public final int skillEffect4;
    public final int skillEffect5;
    public final int skillEffect6;

    public final int penalty0;
    public final int penalty1;
    public final int penalty2;
    public final int penalty3;
    public final int penalty4;
    public final int penalty5;
    public final int penalty6;

    SkillType(int experienceReq0, int experienceReq1, int experienceReq2, int experienceReq3, int experienceReq4, int experienceReq5, int experienceReq6, int cooldown0, int cooldown1, int cooldown2, int cooldown3, int cooldown4, int cooldown5, int cooldown6, int skillEffect0, int skillEffect1, int skillEffect2, int skillEffect3, int skillEffect4, int skillEffect5, int skillEffect6, int penalty0, int penalty1, int penalty2, int penalty3, int penalty4, int penalty5, int penalty6){
        this.experienceReq0 = experienceReq0;
        this.experienceReq1 = experienceReq1;
        this.experienceReq2 = experienceReq2;
        this.experienceReq3 = experienceReq3;
        this.experienceReq4 = experienceReq4;
        this.experienceReq5 = experienceReq5;
        this.experienceReq6 = experienceReq6;
        this.cooldown0 = cooldown0;
        this.cooldown1 = cooldown1;
        this.cooldown2 = cooldown2;
        this.cooldown3 = cooldown3;
        this.cooldown4 = cooldown4;
        this.cooldown5 = cooldown5;
        this.cooldown6 = cooldown6;
        this.skillEffect0 = skillEffect0;
        this.skillEffect1 = skillEffect1;
        this.skillEffect2 = skillEffect2;
        this.skillEffect3 = skillEffect3;
        this.skillEffect4 = skillEffect4;
        this.skillEffect5 = skillEffect5;
        this.skillEffect6 = skillEffect6;
        this.penalty0 = penalty0;
        this.penalty1 = penalty1;
        this.penalty2 = penalty2;
        this.penalty3 = penalty3;
        this.penalty4 = penalty4;
        this.penalty5 = penalty5;
        this.penalty6 = penalty6;
    }

}