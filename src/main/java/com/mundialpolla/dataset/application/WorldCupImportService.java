package com.mundialpolla.dataset.application;

import com.mundialpolla.dataset.infrastructure.json.WorldCupJsonReader;
import com.mundialpolla.dataset.infrastructure.json.dto.WorldCupDatasetDto;
import com.mundialpolla.dataset.infrastructure.json.dto.WorldCupMatchDto;
import com.mundialpolla.dataset.infrastructure.json.dto.WorldCupScoreDto;
import com.mundialpolla.dataset.mapping.WorldCupDateTimeParser;
import com.mundialpolla.dataset.mapping.WorldCupStageMapper;
import com.mundialpolla.groups.domain.GroupTeam;
import com.mundialpolla.groups.domain.TournamentGroup;
import com.mundialpolla.groups.infrastructure.persistence.GroupTeamRepository;
import com.mundialpolla.groups.infrastructure.persistence.TournamentGroupRepository;
import com.mundialpolla.matches.domain.Match;
import com.mundialpolla.matches.domain.MatchStatus;
import com.mundialpolla.matches.infrastructure.persistence.MatchRepository;
import com.mundialpolla.shared.config.PredictionPolicy;
import com.mundialpolla.stages.domain.Stage;
import com.mundialpolla.stages.domain.StageType;
import com.mundialpolla.stages.infrastructure.persistence.StageRepository;
import com.mundialpolla.teams.domain.Team;
import com.mundialpolla.teams.infrastructure.persistence.TeamRepository;
import com.mundialpolla.tournaments.domain.Tournament;
import com.mundialpolla.tournaments.infrastructure.persistence.TournamentRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorldCupImportService {

    private static final long TOURNAMENT_EXTERNAL_ID = 2026L;
    private static final long TEAM_EXTERNAL_ID_START = 2026201L;
    private static final long MATCH_WITHOUT_NUM_EXTERNAL_ID_START = 2026001L;
    private static final String SEASON = "2026";

    private static final Map<StageType, StageDefinition> STAGES = new EnumMap<>(StageType.class);

    static {
        STAGES.put(StageType.GROUP_STAGE, new StageDefinition(202601L, "Group Stage", 1));
        STAGES.put(StageType.ROUND_OF_32, new StageDefinition(202602L, "Round of 32", 2));
        STAGES.put(StageType.ROUND_OF_16, new StageDefinition(202603L, "Round of 16", 3));
        STAGES.put(StageType.QUARTER_FINAL, new StageDefinition(202604L, "Quarter-final", 4));
        STAGES.put(StageType.SEMI_FINAL, new StageDefinition(202605L, "Semi-final", 5));
        STAGES.put(StageType.THIRD_PLACE, new StageDefinition(202606L, "Match for third place", 6));
        STAGES.put(StageType.FINAL, new StageDefinition(202607L, "Final", 7));
    }

    private final WorldCupJsonReader worldCupJsonReader;
    private final TournamentRepository tournamentRepository;
    private final TeamRepository teamRepository;
    private final StageRepository stageRepository;
    private final TournamentGroupRepository tournamentGroupRepository;
    private final GroupTeamRepository groupTeamRepository;
    private final MatchRepository matchRepository;
    private final WorldCupDateTimeParser dateTimeParser;
    private final WorldCupStageMapper stageMapper;

    public WorldCupImportService(
            WorldCupJsonReader worldCupJsonReader,
            TournamentRepository tournamentRepository,
            TeamRepository teamRepository,
            StageRepository stageRepository,
            TournamentGroupRepository tournamentGroupRepository,
            GroupTeamRepository groupTeamRepository,
            MatchRepository matchRepository,
            WorldCupDateTimeParser dateTimeParser,
            WorldCupStageMapper stageMapper
    ) {
        this.worldCupJsonReader = worldCupJsonReader;
        this.tournamentRepository = tournamentRepository;
        this.teamRepository = teamRepository;
        this.stageRepository = stageRepository;
        this.tournamentGroupRepository = tournamentGroupRepository;
        this.groupTeamRepository = groupTeamRepository;
        this.matchRepository = matchRepository;
        this.dateTimeParser = dateTimeParser;
        this.stageMapper = stageMapper;
    }

    public WorldCupImportResult importDataset() {
        WorldCupDatasetDto dataset = worldCupJsonReader.read();
        List<PreparedMatch> matches = prepareMatches(dataset.matches());
        ImportCounters counters = new ImportCounters();

        Tournament tournament = importTournament(dataset, matches, counters);
        Map<String, Team> teamsByName = importTeams(matches, counters);
        Map<StageType, Stage> stagesByType = importStages(tournament, matches, counters);
        Map<String, TournamentGroup> groupsByName = importGroups(tournament, stagesByType, matches, counters);
        importGroupTeams(matches, teamsByName, groupsByName, counters);
        importMatches(tournament, matches, teamsByName, stagesByType, groupsByName, counters);

        return counters.toResult();
    }

    private List<PreparedMatch> prepareMatches(List<WorldCupMatchDto> matchDtos) {
        List<PreparedMatch> preparedMatches = new ArrayList<>();
        for (WorldCupMatchDto matchDto : matchDtos) {
            String team1 = normalizeRequired(matchDto.team1(), "team1");
            String team2 = normalizeRequired(matchDto.team2(), "team2");
            if (team1.equalsIgnoreCase(team2)) {
                throw invalidMatch(matchDto, null, "team1 and team2 must be different");
            }

            Instant startsAt = dateTimeParser.parse(matchDto.date(), matchDto.time());
            StageType stageType = stageMapper.map(matchDto.round(), matchDto.group());
            ScoreValues score = scoreValues(matchDto);
            preparedMatches.add(new PreparedMatch(
                    matchDto,
                    team1,
                    team2,
                    normalizeOptional(matchDto.group()),
                    stageType,
                    startsAt,
                    score
            ));
        }

        assignMatchExternalIds(preparedMatches);
        return preparedMatches;
    }

    private void assignMatchExternalIds(List<PreparedMatch> matches) {
        Set<Long> externalIds = new HashSet<>();
        for (PreparedMatch match : matches) {
            if (match.dto().num() != null) {
                match.setExternalId(2026000L + match.dto().num());
                addMatchExternalId(externalIds, match);
            }
        }

        matches.stream()
                .filter(match -> match.dto().num() == null)
                .sorted(Comparator
                        .comparing(PreparedMatch::startsAt)
                        .thenComparing(PreparedMatch::team1, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(PreparedMatch::team2, String.CASE_INSENSITIVE_ORDER))
                .forEach(match -> {
                    long nextExternalId = MATCH_WITHOUT_NUM_EXTERNAL_ID_START;
                    while (externalIds.contains(nextExternalId)) {
                        nextExternalId++;
                    }
                    match.setExternalId(nextExternalId);
                    addMatchExternalId(externalIds, match);
                });
    }

    private void addMatchExternalId(Set<Long> externalIds, PreparedMatch match) {
        if (!externalIds.add(match.externalId())) {
            throw invalidMatch(match.dto(), match.externalId(), "duplicated match externalId");
        }
    }

    private Tournament importTournament(WorldCupDatasetDto dataset, List<PreparedMatch> matches, ImportCounters counters) {
        String name = normalizeRequired(dataset.name(), "name");
        LocalDate startDate = matches.stream()
                .map(match -> LocalDate.parse(match.dto().date()))
                .min(LocalDate::compareTo)
                .orElseThrow(() -> new IllegalStateException("dataset without matches"));
        LocalDate endDate = matches.stream()
                .map(match -> LocalDate.parse(match.dto().date()))
                .max(LocalDate::compareTo)
                .orElseThrow(() -> new IllegalStateException("dataset without matches"));

        return tournamentRepository.findByExternalId(TOURNAMENT_EXTERNAL_ID)
                .map(existing -> {
                    if (!tournamentMatches(existing, name, startDate, endDate) || !existing.isActive()) {
                        existing.updateDetails(name, SEASON, startDate, endDate);
                        existing.activate();
                        counters.tournamentsUpdated++;
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    Tournament tournament = new Tournament(TOURNAMENT_EXTERNAL_ID, name, SEASON, startDate, endDate);
                    tournament.activate();
                    counters.tournamentsCreated++;
                    return tournamentRepository.save(tournament);
                });
    }

    private Map<String, Team> importTeams(List<PreparedMatch> matches, ImportCounters counters) {
        Set<String> teamNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        matches.forEach(match -> {
            teamNames.add(match.team1());
            teamNames.add(match.team2());
        });

        Map<String, Team> teamsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        long externalId = TEAM_EXTERNAL_ID_START;
        for (String teamName : teamNames) {
            Long teamExternalId = externalId++;
            Team team = teamRepository.findByExternalId(teamExternalId)
                    .map(existing -> {
                        if (!teamMatches(existing, teamName) || !existing.isActive()) {
                            existing.updateDetails(teamName, null, null, null, null);
                            existing.activate();
                            counters.teamsUpdated++;
                        }
                        return existing;
                    })
                    .orElseGet(() -> {
                        counters.teamsCreated++;
                        return teamRepository.save(new Team(teamExternalId, teamName, null, null, null, null));
                    });
            teamsByName.put(teamName, team);
        }
        return teamsByName;
    }

    private Map<StageType, Stage> importStages(Tournament tournament, List<PreparedMatch> matches, ImportCounters counters) {
        Set<StageType> requiredTypes = new TreeSet<>(Comparator.comparingInt(type -> stageDefinition(type).orderNumber()));
        matches.stream()
                .map(PreparedMatch::stageType)
                .filter(type -> type != StageType.OTHER)
                .forEach(requiredTypes::add);

        Map<StageType, Stage> stagesByType = new EnumMap<>(StageType.class);
        for (StageType type : requiredTypes) {
            StageDefinition definition = stageDefinition(type);
            Stage stage = stageRepository.findByExternalId(definition.externalId())
                    .map(existing -> {
                        if (!stageMatches(existing, definition, type) || !existing.isActive()) {
                            existing.updateDetails(definition.name(), type, definition.orderNumber());
                            existing.activate();
                            counters.stagesUpdated++;
                        }
                        return existing;
                    })
                    .orElseGet(() -> {
                        counters.stagesCreated++;
                        return stageRepository.save(new Stage(tournament, definition.externalId(), definition.name(), type, definition.orderNumber()));
                    });
            stagesByType.put(type, stage);
        }
        return stagesByType;
    }

    private Map<String, TournamentGroup> importGroups(
            Tournament tournament,
            Map<StageType, Stage> stagesByType,
            List<PreparedMatch> matches,
            ImportCounters counters
    ) {
        Stage groupStage = stagesByType.get(StageType.GROUP_STAGE);
        Map<String, TournamentGroup> groupsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Set<String> groupNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        matches.stream()
                .map(PreparedMatch::group)
                .filter(Objects::nonNull)
                .forEach(groupNames::add);

        for (String groupName : groupNames) {
            GroupDefinition definition = groupDefinition(groupName);
            TournamentGroup group = tournamentGroupRepository.findByExternalId(definition.externalId())
                    .map(existing -> {
                        if (!groupMatches(existing, definition)) {
                            existing.updateDetails(definition.name(), definition.code(), definition.orderNumber());
                            counters.groupsUpdated++;
                        }
                        return existing;
                    })
                    .orElseGet(() -> {
                        counters.groupsCreated++;
                        return tournamentGroupRepository.save(new TournamentGroup(
                                tournament,
                                groupStage,
                                definition.externalId(),
                                definition.name(),
                                definition.code(),
                                definition.orderNumber()
                        ));
                    });
            groupsByName.put(groupName, group);
        }
        return groupsByName;
    }

    private void importGroupTeams(
            List<PreparedMatch> matches,
            Map<String, Team> teamsByName,
            Map<String, TournamentGroup> groupsByName,
            ImportCounters counters
    ) {
        Map<String, Set<String>> teamNamesByGroup = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (PreparedMatch match : matches) {
            if (match.group() == null) {
                continue;
            }
            teamNamesByGroup.computeIfAbsent(match.group(), ignored -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)).add(match.team1());
            teamNamesByGroup.get(match.group()).add(match.team2());
        }

        for (Map.Entry<String, Set<String>> entry : teamNamesByGroup.entrySet()) {
            TournamentGroup group = groupsByName.get(entry.getKey());
            int orderNumber = 1;
            for (String teamName : entry.getValue()) {
                Team team = teamByName(teamsByName, teamName);
                if (!groupTeamRepository.existsByGroupIdAndTeamId(group.getId(), team.getId())) {
                    groupTeamRepository.save(new GroupTeam(group, team, orderNumber));
                    counters.groupTeamsCreated++;
                }
                orderNumber++;
            }
        }
    }

    private void importMatches(
            Tournament tournament,
            List<PreparedMatch> matches,
            Map<String, Team> teamsByName,
            Map<StageType, Stage> stagesByType,
            Map<String, TournamentGroup> groupsByName,
            ImportCounters counters
    ) {
        for (PreparedMatch preparedMatch : matches) {
            Team homeTeam = teamByName(teamsByName, preparedMatch.team1());
            Team awayTeam = teamByName(teamsByName, preparedMatch.team2());
            Stage stage = stageByType(stagesByType, preparedMatch.stageType(), preparedMatch);
            TournamentGroup group = preparedMatch.group() == null ? null : groupsByName.get(preparedMatch.group());
            Team qualifiedTeam = qualifiedTeam(preparedMatch, homeTeam, awayTeam);
            Instant predictionClosesAt = preparedMatch.startsAt().minus(PredictionPolicy.PREDICTION_CLOSE_BEFORE);

            matchRepository.findByExternalId(preparedMatch.externalId())
                    .ifPresentOrElse(existing -> {
                        if (matchMatches(existing, tournament, stage, group, homeTeam, awayTeam, preparedMatch, predictionClosesAt, qualifiedTeam)) {
                            counters.matchesUnchanged++;
                            return;
                        }
                        updateExistingMatch(existing, tournament, stage, group, homeTeam, awayTeam, preparedMatch, predictionClosesAt, qualifiedTeam);
                        counters.matchesUpdated++;
                    }, () -> {
                        Match match = new Match(
                                tournament,
                                stage,
                                group,
                                preparedMatch.externalId(),
                                homeTeam,
                                awayTeam,
                                preparedMatch.startsAt(),
                                predictionClosesAt
                        );
                        confirmResult(match, preparedMatch, qualifiedTeam);
                        matchRepository.save(match);
                        counters.matchesCreated++;
                    });
        }
    }

    private void updateExistingMatch(
            Match existing,
            Tournament tournament,
            Stage stage,
            TournamentGroup group,
            Team homeTeam,
            Team awayTeam,
            PreparedMatch preparedMatch,
            Instant predictionClosesAt,
            Team qualifiedTeam
    ) {
        if (!sameEntity(existing.getTournament(), tournament)) {
            throw invalidMatch(preparedMatch.dto(), preparedMatch.externalId(), "historical tournament mismatch");
        }
        if (!scheduleMatches(existing, stage, group, homeTeam, awayTeam, preparedMatch.startsAt(), predictionClosesAt)) {
            try {
                existing.updateSchedule(stage, group, homeTeam, awayTeam, preparedMatch.startsAt(), predictionClosesAt);
            } catch (IllegalStateException exception) {
                throw invalidMatch(preparedMatch.dto(), preparedMatch.externalId(), "historical schedule mismatch");
            }
        }
        if (!resultMatches(existing, preparedMatch, qualifiedTeam)) {
            confirmResult(existing, preparedMatch, qualifiedTeam);
        }
    }

    private void confirmResult(Match match, PreparedMatch preparedMatch, Team qualifiedTeam) {
        match.confirmResult(
                preparedMatch.score().homeScore(),
                preparedMatch.score().awayScore(),
                preparedMatch.score().homePenaltyScore(),
                preparedMatch.score().awayPenaltyScore(),
                qualifiedTeam,
                preparedMatch.startsAt()
        );
    }

    private Team qualifiedTeam(PreparedMatch match, Team homeTeam, Team awayTeam) {
        if (match.stageType() == StageType.GROUP_STAGE) {
            return null;
        }

        ScoreValues score = match.score();
        int homeFinalScore = score.homePenaltyScore() != null ? score.homePenaltyScore() : score.homeScore();
        int awayFinalScore = score.awayPenaltyScore() != null ? score.awayPenaltyScore() : score.awayScore();
        if (homeFinalScore == awayFinalScore) {
            throw invalidMatch(match.dto(), match.externalId(), "knockout match has tied final available score");
        }
        return homeFinalScore > awayFinalScore ? homeTeam : awayTeam;
    }

    private ScoreValues scoreValues(WorldCupMatchDto match) {
        WorldCupScoreDto score = match.score();
        if (score == null) {
            throw invalidMatch(match, null, "score is required");
        }
        List<Integer> ft = scorePair(score.ft(), "ft", match);
        List<Integer> et = score.et() == null ? null : scorePair(score.et(), "et", match);
        List<Integer> penalties = score.p() == null ? null : scorePair(score.p(), "p", match);

        List<Integer> officialScore = et == null ? ft : et;
        return new ScoreValues(
                officialScore.get(0),
                officialScore.get(1),
                penalties == null ? null : penalties.get(0),
                penalties == null ? null : penalties.get(1)
        );
    }

    private List<Integer> scorePair(List<Integer> values, String fieldName, WorldCupMatchDto match) {
        if (values == null || values.size() != 2) {
            throw invalidMatch(match, null, "invalid score." + fieldName);
        }
        if (values.get(0) == null || values.get(1) == null || values.get(0) < 0 || values.get(1) < 0) {
            throw invalidMatch(match, null, "invalid score." + fieldName);
        }
        return values;
    }

    private Stage stageByType(Map<StageType, Stage> stagesByType, StageType type, PreparedMatch match) {
        Stage stage = stagesByType.get(type);
        if (stage == null) {
            throw invalidMatch(match.dto(), match.externalId(), "stage not found during match mapping");
        }
        return stage;
    }

    private Team teamByName(Map<String, Team> teamsByName, String teamName) {
        Team team = teamsByName.get(teamName);
        if (team == null) {
            throw new IllegalStateException("team not found during match mapping: " + teamName);
        }
        return team;
    }

    private GroupDefinition groupDefinition(String groupName) {
        String normalizedGroupName = normalizeRequired(groupName, "group");
        if (!normalizedGroupName.regionMatches(true, 0, "Group ", 0, 6) || normalizedGroupName.length() != 7) {
            throw new IllegalStateException("unknown group code: " + groupName);
        }

        char code = Character.toUpperCase(normalizedGroupName.charAt(6));
        if (code < 'A' || code > 'L') {
            throw new IllegalStateException("unknown group code: " + groupName);
        }

        int orderNumber = code - 'A' + 1;
        return new GroupDefinition(2026100L + orderNumber, "Group " + code, String.valueOf(code), orderNumber);
    }

    private StageDefinition stageDefinition(StageType type) {
        StageDefinition definition = STAGES.get(type);
        if (definition == null) {
            throw new IllegalStateException("unsupported stage type for import: " + type);
        }
        return definition;
    }

    private IllegalStateException invalidMatch(WorldCupMatchDto match, Long externalId, String message) {
        return new IllegalStateException(message
                + " [externalId=" + externalId
                + ", team1=" + match.team1()
                + ", team2=" + match.team2()
                + ", date=" + match.date()
                + "]");
    }

    private boolean tournamentMatches(Tournament tournament, String name, LocalDate startDate, LocalDate endDate) {
        return Objects.equals(tournament.getName(), name)
                && Objects.equals(tournament.getSeason(), SEASON)
                && Objects.equals(tournament.getStartDate(), startDate)
                && Objects.equals(tournament.getEndDate(), endDate);
    }

    private boolean teamMatches(Team team, String name) {
        return Objects.equals(team.getName(), name)
                && team.getShortName() == null
                && team.getCode() == null
                && team.getCountryCode() == null
                && team.getLogoUrl() == null;
    }

    private boolean stageMatches(Stage stage, StageDefinition definition, StageType type) {
        return Objects.equals(stage.getName(), definition.name())
                && stage.getType() == type
                && stage.getOrderNumber() == definition.orderNumber();
    }

    private boolean groupMatches(TournamentGroup group, GroupDefinition definition) {
        return Objects.equals(group.getName(), definition.name())
                && Objects.equals(group.getCode(), definition.code())
                && group.getOrderNumber() == definition.orderNumber();
    }

    private boolean matchMatches(
            Match match,
            Tournament tournament,
            Stage stage,
            TournamentGroup group,
            Team homeTeam,
            Team awayTeam,
            PreparedMatch preparedMatch,
            Instant predictionClosesAt,
            Team qualifiedTeam
    ) {
        return sameEntity(match.getTournament(), tournament)
                && scheduleMatches(match, stage, group, homeTeam, awayTeam, preparedMatch.startsAt(), predictionClosesAt)
                && resultMatches(match, preparedMatch, qualifiedTeam);
    }

    private boolean scheduleMatches(
            Match match,
            Stage stage,
            TournamentGroup group,
            Team homeTeam,
            Team awayTeam,
            Instant startsAt,
            Instant predictionClosesAt
    ) {
        return sameEntity(match.getStage(), stage)
                && sameEntity(match.getGroup(), group)
                && sameEntity(match.getHomeTeam(), homeTeam)
                && sameEntity(match.getAwayTeam(), awayTeam)
                && Objects.equals(match.getStartsAt(), startsAt)
                && Objects.equals(match.getPredictionClosesAt(), predictionClosesAt);
    }

    private boolean resultMatches(Match match, PreparedMatch preparedMatch, Team qualifiedTeam) {
        return match.getStatus() == MatchStatus.FINISHED
                && Objects.equals(match.getHomeScore(), preparedMatch.score().homeScore())
                && Objects.equals(match.getAwayScore(), preparedMatch.score().awayScore())
                && Objects.equals(match.getHomePenaltyScore(), preparedMatch.score().homePenaltyScore())
                && Objects.equals(match.getAwayPenaltyScore(), preparedMatch.score().awayPenaltyScore())
                && sameEntity(match.getQualifiedTeam(), qualifiedTeam)
                && Objects.equals(match.getResultConfirmedAt(), preparedMatch.startsAt());
    }

    private boolean sameEntity(Object first, Object second) {
        if (first == second) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        if (first instanceof Stage firstStage && second instanceof Stage secondStage) {
            return Objects.equals(firstStage.getId(), secondStage.getId());
        }
        if (first instanceof TournamentGroup firstGroup && second instanceof TournamentGroup secondGroup) {
            return Objects.equals(firstGroup.getId(), secondGroup.getId());
        }
        if (first instanceof Team firstTeam && second instanceof Team secondTeam) {
            return Objects.equals(firstTeam.getId(), secondTeam.getId());
        }
        if (first instanceof Tournament firstTournament && second instanceof Tournament secondTournament) {
            return Objects.equals(firstTournament.getId(), secondTournament.getId());
        }
        return false;
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private record StageDefinition(Long externalId, String name, int orderNumber) {
    }

    private record GroupDefinition(Long externalId, String name, String code, int orderNumber) {
    }

    private record ScoreValues(
            int homeScore,
            int awayScore,
            Integer homePenaltyScore,
            Integer awayPenaltyScore
    ) {
    }

    private static final class PreparedMatch {
        private final WorldCupMatchDto dto;
        private final String team1;
        private final String team2;
        private final String group;
        private final StageType stageType;
        private final Instant startsAt;
        private final ScoreValues score;
        private Long externalId;

        private PreparedMatch(
                WorldCupMatchDto dto,
                String team1,
                String team2,
                String group,
                StageType stageType,
                Instant startsAt,
                ScoreValues score
        ) {
            this.dto = dto;
            this.team1 = team1;
            this.team2 = team2;
            this.group = group;
            this.stageType = stageType;
            this.startsAt = startsAt;
            this.score = score;
        }

        private WorldCupMatchDto dto() {
            return dto;
        }

        private String team1() {
            return team1;
        }

        private String team2() {
            return team2;
        }

        private String group() {
            return group;
        }

        private StageType stageType() {
            return stageType;
        }

        private Instant startsAt() {
            return startsAt;
        }

        private ScoreValues score() {
            return score;
        }

        private Long externalId() {
            return externalId;
        }

        private void setExternalId(Long externalId) {
            this.externalId = externalId;
        }
    }

    private static final class ImportCounters {
        private int tournamentsCreated;
        private int tournamentsUpdated;
        private int teamsCreated;
        private int teamsUpdated;
        private int stagesCreated;
        private int stagesUpdated;
        private int groupsCreated;
        private int groupsUpdated;
        private int groupTeamsCreated;
        private int matchesCreated;
        private int matchesUpdated;
        private int matchesUnchanged;

        private WorldCupImportResult toResult() {
            return new WorldCupImportResult(
                    tournamentsCreated,
                    tournamentsUpdated,
                    teamsCreated,
                    teamsUpdated,
                    stagesCreated,
                    stagesUpdated,
                    groupsCreated,
                    groupsUpdated,
                    groupTeamsCreated,
                    matchesCreated,
                    matchesUpdated,
                    matchesUnchanged
            );
        }
    }
}
