package net.minespire.landclaim.Claim;

import org.bukkit.configuration.ConfigurationSection;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class VoteRegion {

    private String regionCommaWorld;
    private String regionName;
    private String regionWorld;
    private int votesThisYear;
    private int votesThisMonth;
    private int votesToday;
    private static List<VoteRegion> voteRegionList = new ArrayList<>();

    public VoteRegion(final String regionCommaWorld) {
        this.regionCommaWorld = regionCommaWorld;
        this.regionName = regionCommaWorld.split(",")[0];
        this.regionWorld = regionCommaWorld.split(",")[1];
        this.votesThisYear = 0;
        this.votesThisMonth = 0;
        this.votesToday = 0;
    }

    public static void tallyAllVotes() {
        final VoteFile voteFile = VoteFile.get();
        voteRegionList.clear();
        for (final String region : voteFile.getYml().getKeys(false)) {
            final ConfigurationSection regionSection = voteFile.getYml().getConfigurationSection(region);
            final VoteRegion voteRegion = new VoteRegion(region);
            for (final String vote : regionSection.getConfigurationSection("votes").getKeys(false)) {
                countVotes(voteRegion, vote);
            }
            voteRegionList.add(voteRegion);
        }
    }

    public static void tallyVotesFor(final String regionCommaWorld) {
        final VoteFile voteFile = VoteFile.get();
        final ConfigurationSection regionSection = voteFile.getYml().getConfigurationSection(regionCommaWorld);
        final VoteRegion voteRegion = voteRegionList
            .stream()
            .filter(a -> a.getRegionCommaWorld().equals(regionCommaWorld))
            .findFirst()
            .orElseGet(() -> {
                final VoteRegion vRegion = new VoteRegion(regionCommaWorld);
                voteRegionList.add(vRegion);
                return vRegion;
            });
        voteRegion.clearVotes();
        for (final String vote : regionSection.getConfigurationSection("votes").getKeys(false)) {
            countVotes(voteRegion, vote);
        }
    }

    private void clearVotes() {
        votesToday = 0;
        votesThisMonth = 0;
        votesThisYear = 0;
    }

    public static void addVote(final String regionCommaWorld) {
        final VoteRegion voteRegion = voteRegionList
            .stream()
            .filter(a -> a.getRegionCommaWorld().equals(regionCommaWorld))
            .findFirst()
            .orElseGet(() -> {
                final VoteRegion vRegion = new VoteRegion(regionCommaWorld);
                voteRegionList.add(vRegion);
                return vRegion;
            });
        voteRegion.votesThisYear++;
        voteRegion.votesThisMonth++;
        voteRegion.votesToday++;
    }

    private static void countVotes(final VoteRegion voteRegion, final String vote) {
        final LocalDateTime voteDateTime = LocalDateTime.parse(vote);
        final LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        if (Duration.between(voteDateTime, now).toDays() < 1) {
            voteRegion.votesToday++;
            voteRegion.votesThisMonth++;
            voteRegion.votesThisYear++;
        } else if (Duration.between(voteDateTime, now).toDays() < voteDateTime.getMonth().length(false)) {
            voteRegion.votesThisMonth++;
            voteRegion.votesThisYear++;
        } else if (Duration.between(voteDateTime, now).toDays() < 365) {
            voteRegion.votesThisYear++;
        }
    }

    public static List<VoteRegion> getVoteRegionList() {
        return voteRegionList;
    }

    public int getVotesThisYear() {
        return votesThisYear;
    }

    public int getVotesThisMonth() {
        return votesThisMonth;
    }

    public int getVotesToday() {
        return votesToday;
    }

    public void addVote() {
        votesThisYear++;
        votesThisMonth++;
        votesToday++;
    }

    public String getRegionCommaWorld() {
        return regionCommaWorld;
    }

    public String getRegionName() {
        return regionName;
    }

    public String getRegionWorld() {
        return regionWorld;
    }
}
