package battlecode.common;

/**
 * Enumerates the different skill types a unit can specialize in.
 */

public enum SkillType{

    // base skill effect, base cooldown - build is 0 because variable
    /**
     * Attacking immediately reduces one enemy unit's (in range) base health by 15 (level 0), with a cooldown cost of 20.
     *
     * @battlecode.doc.skilltype
     */
    ATTACK(-15, 20),
    
    /**
     * Build uses team resources to dig, fill, or build a trap.
     *
     * @battlecode.doc.skilltype
     */
    BUILD(0, 0),
    
    /**
     * Healing adds 8 health points to a nearby friendly unit, with a cooldown cost of 30. (level 0)
     * 
     * @battlecode.doc.skilltype
     */
    HEAL(8, 30);

    public int skillEffect;
    public int cooldown;


    /**
     * Returns the number of experience points needed to achieve each level
     * 
     * @return number of experience points needed to achieve each level
     * @battlecode.doc.costlymethod
     */
    public int getExperience(int level){
        int[] attackExperience = {0, 25, 50, 75, 125, 175, 250};
        int[] buildExperience = {0, 10, 20, 30, 50, 75, 125};
        int[] healExperience = {0, 10, 20, 30, 50, 75, 125};
        switch(this){
            case ATTACK: return attackExperience[level];
            case BUILD: return buildExperience[level];
            case HEAL: return healExperience[level];
        }
        return 0;
    }

    // NOTE: These are using percentages except for attack, standardize later
    /**
     * Returns the change in cooldown for each level
     * 
     * @return change in cooldown for each level
     * @battlecode.doc.costlymethod
     */
    public int getCooldown(int level){
        int[] attackCooldown = {0, -1, -2, -3, -3, -6, -10};
        int[] buildCooldown = {0, -5, -10, -15, -20, -30, -50};
        int[] healCooldown = {0, -5, -10, -15, -15, -15, -25};
        switch(this){
            case ATTACK: return attackCooldown[level];
            case BUILD: return buildCooldown[level];
            case HEAL: return healCooldown[level];
        }
        return 0;
    }

    // ALL PERCENTAGES RN
    /**
     * Returns the change in skill effect for each level
     * 
     * @return change in skill effect for each level
     * @battlecode.doc.costlymethod
     */
    public int getSkillEffect(int level){
        int[] attackSkill = {0, 5, 10, 15, 20, 30, 50};
        int[] buildSkill = {0, 0, 0, 0, 0, 0, 0};
        int[] healSkill = {0, 3, 5, 7, 10, 15, 25};
        switch(this){
            case ATTACK: return attackSkill[level];
            case BUILD: return buildSkill[level];
            case HEAL: return healSkill[level];
        }
        return 0;
    }

    /**
     * Returns the penalty of experience points from being jailed in each level
     * 
     * @return penalty of experience points from being jailed in each level
     * @battlecode.doc.costlymethod
     */
    public int getPenalty(int level){
        int[] attackPenalty = {-1, -5, -5, -10, -10, -15, -15};
        int[] buildPenalty = {-1, -2, -2, -5, -5, -10, -10};
        int[] healPenalty = {-1, -2, -2, -5, -5, -10, -10};
        switch(this){
            case ATTACK: return attackPenalty[level];
            case BUILD: return buildPenalty[level];
            case HEAL: return healPenalty[level];
        }
        return 0;
    }

    SkillType(int skillEffect, int cooldown){
        this.skillEffect = skillEffect;
        this.cooldown = cooldown;
    }

}