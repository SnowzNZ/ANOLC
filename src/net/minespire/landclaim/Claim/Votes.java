package net.minespire.landclaim.Claim;

import org.bukkit.configuration.ConfigurationSection;

import java.time.Duration;
import java.time.LocalDateTime;

public class Votes {


    public static void tallyVotes(){
        VoteFile voteFile = VoteFile.get();

        for(String region : voteFile.getYml().getKeys(false)){
            ConfigurationSection regionSection = voteFile.getYml().getConfigurationSection(region);
            int votesThisYear = 0;
            int votesThisMonth = 0;
            int votesToday = 0;
            for(String vote : regionSection.getConfigurationSection("votes").getKeys(false)){
                LocalDateTime dateTime = LocalDateTime.parse(vote);
                LocalDateTime now = LocalDateTime.now();


                if(dateTime.getDayOfMonth() == now.getDayOfMonth() && Duration.between(dateTime, now).toDays() < 1){
                    votesToday++;
                    votesThisMonth++;
                    votesThisYear++;
                } else if(dateTime.getMonth() == now.getMonth() && Duration.between(dateTime, now).toDays() < now.getMonth().maxLength()){
                    votesThisMonth++;
                    votesThisYear++;
                } else if(dateTime.getYear() == now.getYear()){
                    votesThisYear++;
                }
            }
            regionSection.set("year", votesThisYear);
            regionSection.set("month", votesThisMonth);
            regionSection.set("day", votesToday);
            voteFile.save();
        }
    }

}
