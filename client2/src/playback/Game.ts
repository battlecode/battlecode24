import Match from './Match';

export default class Game {
    private readonly rounds: Match[] = [];
    private readonly teams: [Team, Team];
}

export type Team = {
    name: string;
    stats: TeamStat;
};

export type TeamStat = {
    wins: number;
    elo: number;
};