import Round from './Round';

export default class Game {
    private readonly rounds: Round[] = [];
    private readonly teams: Map<Team, TeamStat> = new Map();
}

export type Team = {
    name: string;
    id: number;
    local_id: number;
};

export type TeamStat = {
    wins: number;
    elo: number;
};